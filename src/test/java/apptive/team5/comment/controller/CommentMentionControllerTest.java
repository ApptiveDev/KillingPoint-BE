package apptive.team5.comment.controller;

import apptive.team5.comment.domain.CommentEntity;
import apptive.team5.comment.domain.MentionTargetType;
import apptive.team5.comment.dto.CommentCreateRequest;
import apptive.team5.comment.dto.CommentResponse;
import apptive.team5.comment.dto.CommentUpdateRequest;
import apptive.team5.comment.dto.MentionReferenceRequest;
import apptive.team5.comment.dto.MentionReferenceResponse;
import apptive.team5.comment.repository.CommentMentionRepository;
import apptive.team5.comment.repository.CommentRepository;
import apptive.team5.comment.service.CommentCleanupService;
import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.repository.DiaryRepository;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.user.domain.SocialType;
import apptive.team5.user.domain.UserBlock;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserRoleType;
import apptive.team5.user.repository.UserBlockRepository;
import apptive.team5.user.repository.UserRepository;
import apptive.team5.util.TestSecurityContextHolderInjection;
import apptive.team5.util.TestUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentMentionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EntityManager em;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DiaryRepository diaryRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private CommentMentionRepository commentMentionRepository;
    @Autowired
    private UserBlockRepository userBlockRepository;
    @Autowired
    private CommentCleanupService commentCleanupService;

    private UserEntity writer;
    private UserEntity target;
    private DiaryEntity diary;

    @BeforeEach
    void setUp() {
        writer = userRepository.save(TestUtil.makeUserEntity());
        target = userRepository.save(TestUtil.makeDifferentUserEntity(writer));
        diary = diaryRepository.save(TestUtil.makeDiaryEntity(writer));
        loginAs(writer);
    }

    @Test
    @DisplayName("멘션 포함 댓글 작성 - attachment 에 atOrders, len, display 가 담긴다")
    void createCommentWithMention() throws Exception {
        String text = "@kim @kim 좋은 노래";
        MentionReferenceRequest reference = new MentionReferenceRequest(MentionTargetType.USER, target.getId(), List.of(1, 2), 3);

        CommentResponse response = objectMapper.readValue(
                create(new CommentCreateRequest(text, null, List.of(reference)))
                        .andExpect(status().isCreated())
                        .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                CommentResponse.class);

        MentionReferenceResponse mention = response.attachment().references().getFirst();

        assertSoftly(softly -> {
            softly.assertThat(response.text()).isEqualTo(text);
            softly.assertThat(response.attachment().references()).hasSize(1);
            softly.assertThat(mention.targetType()).isEqualTo(MentionTargetType.USER);
            softly.assertThat(mention.targetId()).isEqualTo(target.getId());
            softly.assertThat(mention.atOrders()).containsExactly(1, 2);
            softly.assertThat(mention.len()).isEqualTo(3);
            softly.assertThat(mention.display().username()).isEqualTo(target.getUsername());
            softly.assertThat(mention.display().tag()).isEqualTo(target.getTag());
            softly.assertThat(commentMentionRepository.findByCommentIds(List.of(response.commentId()))).hasSize(2);
        });
    }

    @Test
    @DisplayName("references 없이 작성하면 attachment 는 빈 목록")
    void createCommentWithoutMention() throws Exception {
        CommentResponse response = objectMapper.readValue(
                create(new CommentCreateRequest("멘션 없음 @그냥텍스트", null, null))
                        .andExpect(status().isCreated())
                        .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                CommentResponse.class);

        assertThat(response.attachment().references()).isEmpty();
    }

    @Test
    @DisplayName("멘션 대상이 6명이면 400")
    void mentionLimitExceeded() throws Exception {
        List<MentionReferenceRequest> references = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        for (int i = 1; i <= 6; i++) {
            UserEntity user = userRepository.save(new UserEntity("mention-" + i, "m" + i + "@test.local", "u" + i,
                    writer.getTag() + "-m" + i, UserRoleType.USER, SocialType.KAKAO));
            text.append("@u").append(i).append(' ');
            references.add(new MentionReferenceRequest(MentionTargetType.USER, user.getId(), List.of(i), 2));
        }

        String body = create(new CommentCreateRequest(text.toString(), null, references))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(objectMapper.readTree(body).path("message").asText())
                .isEqualTo(ExceptionCode.MENTION_LIMIT_EXCEEDED.getDescription());
    }

    @Test
    @DisplayName("atOrder 가 본문의 @ 개수를 넘으면 400")
    void atOrderOutOfRange() throws Exception {
        MentionReferenceRequest reference = new MentionReferenceRequest(MentionTargetType.USER, target.getId(), List.of(2), 3);

        expectBadRequest(create(new CommentCreateRequest("@kim 하나뿐", null, List.of(reference))),
                ExceptionCode.INVALID_MENTION_REFERENCE);
    }

    @Test
    @DisplayName("같은 atOrder 를 두 번 쓰면 400")
    void duplicateAtOrder() throws Exception {
        UserEntity another = userRepository.save(TestUtil.makeDifferentUserEntity(target));
        List<MentionReferenceRequest> references = List.of(
                new MentionReferenceRequest(MentionTargetType.USER, target.getId(), List.of(1), 3),
                new MentionReferenceRequest(MentionTargetType.USER, another.getId(), List.of(1), 3)
        );

        expectBadRequest(create(new CommentCreateRequest("@kim @lee", null, references)),
                ExceptionCode.INVALID_MENTION_REFERENCE);
    }

    @Test
    @DisplayName("len 이 본문 끝을 넘으면 400")
    void lenOverflow() throws Exception {
        MentionReferenceRequest reference = new MentionReferenceRequest(MentionTargetType.USER, target.getId(), List.of(1), 10);

        expectBadRequest(create(new CommentCreateRequest("@kim", null, List.of(reference))),
                ExceptionCode.INVALID_MENTION_REFERENCE);
    }

    @Test
    @DisplayName("DIARY 멘션은 아직 지원하지 않아 400")
    void diaryMentionUnsupported() throws Exception {
        MentionReferenceRequest reference = new MentionReferenceRequest(MentionTargetType.DIARY, diary.getId(), List.of(1), 3);

        expectBadRequest(create(new CommentCreateRequest("@(노래) 추천", null, List.of(reference))),
                ExceptionCode.UNSUPPORTED_MENTION_TARGET);
    }

    @Test
    @DisplayName("차단 관계인 유저는 멘션할 수 없다")
    void blockedTarget() throws Exception {
        userBlockRepository.save(new UserBlock(target, writer));
        MentionReferenceRequest reference = new MentionReferenceRequest(MentionTargetType.USER, target.getId(), List.of(1), 3);

        expectBadRequest(create(new CommentCreateRequest("@kim 안녕", null, List.of(reference))),
                ExceptionCode.BLOCKED_MENTION_TARGET);
    }

    @Test
    @DisplayName("존재하지 않는 유저 멘션은 404")
    void targetNotFound() throws Exception {
        MentionReferenceRequest reference = new MentionReferenceRequest(MentionTargetType.USER, 999_999L, List.of(1), 3);

        create(new CommentCreateRequest("@kim 안녕", null, List.of(reference)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("댓글 수정 시 멘션이 교체된다")
    void updateReplacesMentions() throws Exception {
        UserEntity another = userRepository.save(TestUtil.makeDifferentUserEntity(target));
        CommentResponse created = objectMapper.readValue(
                create(new CommentCreateRequest("@kim 안녕", null,
                        List.of(new MentionReferenceRequest(MentionTargetType.USER, target.getId(), List.of(1), 3))))
                        .andExpect(status().isCreated())
                        .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                CommentResponse.class);

        loginAs(writer);
        CommentUpdateRequest update = new CommentUpdateRequest("@lee 반가워",
                List.of(new MentionReferenceRequest(MentionTargetType.USER, another.getId(), List.of(1), 3)));

        CommentResponse updated = objectMapper.readValue(
                mockMvc.perform(put("/api/comments/{commentId}", created.commentId())
                                .with(securityContext(SecurityContextHolder.getContext()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update)))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                CommentResponse.class);

        assertSoftly(softly -> {
            softly.assertThat(updated.isEdited()).isTrue();
            softly.assertThat(updated.attachment().references()).hasSize(1);
            softly.assertThat(updated.attachment().references().getFirst().targetId()).isEqualTo(another.getId());
            softly.assertThat(commentMentionRepository.findByCommentIds(List.of(created.commentId()))).hasSize(1);
        });
    }

    @Test
    @DisplayName("삭제된 댓글 플레이스홀더는 attachment 가 비어 있다")
    void deletedCommentHidesMentions() throws Exception {
        CommentEntity parent = commentRepository.save(TestUtil.makeCommentEntity(diary, writer, "@kim 안녕"));
        commentMentionRepository.save(new apptive.team5.comment.domain.CommentMentionEntity(parent, MentionTargetType.USER, target.getId(), 1, 3));
        commentRepository.save(TestUtil.makeReplyEntity(diary, target, parent, "답글"));
        parent.delete();
        em.flush();
        em.clear();

        CommentResponse placeholder = getComments().getFirst();

        assertSoftly(softly -> {
            softly.assertThat(placeholder.text()).isNull();
            softly.assertThat(placeholder.attachment().references()).isEmpty();
        });
    }

    @Test
    @DisplayName("멘션 대상이 탈퇴하면 그 멘션은 응답에서 빠진다")
    void withdrawnTargetDisappears() throws Exception {
        CommentEntity comment = commentRepository.save(TestUtil.makeCommentEntity(diary, writer, "@kim 안녕"));
        commentMentionRepository.save(new apptive.team5.comment.domain.CommentMentionEntity(comment, MentionTargetType.USER, target.getId(), 1, 3));
        em.flush();
        em.clear();

        commentCleanupService.handleUserWithdrawal(target.getId());
        em.flush();
        em.clear();

        CommentResponse response = getComments().getFirst();

        assertSoftly(softly -> {
            softly.assertThat(response.text()).isEqualTo("@kim 안녕");
            softly.assertThat(response.attachment().references()).isEmpty();
        });
    }

    private ResultActions create(CommentCreateRequest request) throws Exception {
        loginAs(writer);
        return mockMvc.perform(post("/api/diaries/{diaryId}/comments", diary.getId())
                .with(securityContext(SecurityContextHolder.getContext()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private void expectBadRequest(ResultActions actions, ExceptionCode code) throws Exception {
        String body = actions.andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(objectMapper.readTree(body).path("message").asText()).isEqualTo(code.getDescription());
    }

    private List<CommentResponse> getComments() throws Exception {
        loginAs(writer);
        String body = mockMvc.perform(get("/api/diaries/{diaryId}/comments", diary.getId())
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.convertValue(
                objectMapper.readTree(body).path("comments").path("content"),
                new TypeReference<List<CommentResponse>>() {}
        );
    }

    private void loginAs(UserEntity user) {
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());
    }
}
