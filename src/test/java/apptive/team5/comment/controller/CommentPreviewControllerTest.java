package apptive.team5.comment.controller;

import apptive.team5.comment.domain.CommentEntity;
import apptive.team5.comment.dto.CommentPreviewItem;
import apptive.team5.comment.dto.CommentPreviewResponse;
import apptive.team5.comment.repository.CommentRepository;
import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.repository.DiaryRepository;
import apptive.team5.user.domain.SocialType;
import apptive.team5.user.domain.UserBlock;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserRoleType;
import apptive.team5.user.repository.UserBlockRepository;
import apptive.team5.user.repository.UserRepository;
import apptive.team5.util.TestSecurityContextHolderInjection;
import apptive.team5.util.TestUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentPreviewControllerTest {

    private static final String LONG_CONTENT = "이 댓글은 서른 글자를 넘기기 위해 일부러 길게 작성한 미리보기 테스트용 본문입니다";
    private static final String GRINNING_FACE = "😀";

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
    private UserBlockRepository userBlockRepository;

    private UserEntity owner;
    private UserEntity viewer;
    private UserEntity blockedByViewer;
    private UserEntity other;
    private DiaryEntity diary;
    private DiaryEntity emptyDiary;
    private CommentEntity c1;
    private CommentEntity c3;
    private CommentEntity c4;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(TestUtil.makeUserEntity());
        viewer = userRepository.save(new UserEntity("pv-viewer", "v@test.local", "뷰어", "pvviewer", UserRoleType.USER, SocialType.KAKAO));
        blockedByViewer = userRepository.save(new UserEntity("pv-blocked", "b@test.local", "차단됨", "pvblocked", UserRoleType.USER, SocialType.KAKAO));
        other = userRepository.save(new UserEntity("pv-other", "o@test.local", "다른이", "pvother", UserRoleType.USER, SocialType.KAKAO));
        userBlockRepository.save(new UserBlock(viewer, blockedByViewer));

        diary = diaryRepository.save(TestUtil.makeDiaryEntity(owner));
        emptyDiary = diaryRepository.save(TestUtil.makeDiaryEntity(owner));

        c1 = commentRepository.save(TestUtil.makeCommentEntity(diary, other, "첫 번째"));
        commentRepository.save(TestUtil.makeCommentEntity(diary, blockedByViewer, "차단 유저 댓글"));
        c3 = commentRepository.save(TestUtil.makeCommentEntity(diary, owner, LONG_CONTENT));
        c4 = commentRepository.save(TestUtil.makeCommentEntity(diary, viewer, "가장 최신"));
        CommentEntity deleted = commentRepository.save(TestUtil.makeCommentEntity(diary, other, "삭제됨"));
        deleted.delete();
        commentRepository.save(TestUtil.makeReplyEntity(diary, other, c1, "답글은 미포함"));

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("단건 조회 - 최신 3개, 차단 유저와 삭제 댓글 제외, 답글은 개수에 미포함, 본문 30자 절단")
    void previewOnSingleDiary() throws Exception {
        CommentPreviewResponse preview = previewOf(viewer, diary);

        assertSoftly(softly -> {
            softly.assertThat(preview.commentCount()).isEqualTo(4);
            softly.assertThat(preview.comments()).extracting(CommentPreviewItem::commentId)
                    .containsExactly(c4.getId(), c3.getId(), c1.getId());
            softly.assertThat(preview.comments().get(0).userId()).isEqualTo(viewer.getId());
            softly.assertThat(preview.comments().get(0).profileImageUrl()).startsWith("http");
            softly.assertThat(preview.comments().get(1).content()).hasSize(30);
            softly.assertThat(preview.comments().get(2).content()).isEqualTo("첫 번째");
        });
    }

    @Test
    @DisplayName("차단하지 않은 뷰어에게는 차단 유저 댓글도 미리보기에 보인다")
    void previewWithoutBlock() throws Exception {
        CommentPreviewResponse preview = previewOf(other, diary);

        assertThat(preview.comments()).extracting(CommentPreviewItem::userId)
                .containsExactly(viewer.getId(), owner.getId(), blockedByViewer.getId());
    }

    @Test
    @DisplayName("댓글 없는 킬링파트는 commentCount 0 과 빈 목록")
    void previewOnEmptyDiary() throws Exception {
        CommentPreviewResponse preview = previewOf(viewer, emptyDiary);

        assertSoftly(softly -> {
            softly.assertThat(preview.commentCount()).isZero();
            softly.assertThat(preview.comments()).isEmpty();
        });
    }

    @Test
    @DisplayName("30번째 글자가 이모지여도 절단된 미리보기가 깨지지 않는다")
    void previewTruncationIsSurrogateSafe() throws Exception {
        DiaryEntity emojiDiary = diaryRepository.save(TestUtil.makeDiaryEntity(owner));
        commentRepository.save(TestUtil.makeCommentEntity(emojiDiary, other, "a".repeat(29) + GRINNING_FACE + "tail"));
        em.flush();
        em.clear();

        CommentPreviewResponse preview = previewOf(viewer, emojiDiary);
        String content = preview.comments().getFirst().content();

        assertSoftly(softly -> {
            softly.assertThat(content).isEqualTo("a".repeat(29));
            softly.assertThat(content.chars().anyMatch(c -> Character.isSurrogate((char) c))).isFalse();
        });
    }

    @Test
    @DisplayName("상대 컬렉션 목록과 내 컬렉션 목록에도 다이어리별 미리보기가 붙는다")
    void previewOnListEndpoints() throws Exception {
        loginAs(viewer);
        JsonNode userList = objectMapper.readTree(mockMvc.perform(get("/api/diaries/user/{userId}", owner.getId())
                        .param("size", "10")
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        loginAs(owner);
        JsonNode myList = objectMapper.readTree(mockMvc.perform(get("/api/diaries/my")
                        .param("size", "10")
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertSoftly(softly -> {
            softly.assertThat(previewCount(userList, diary.getId())).isEqualTo(4);
            softly.assertThat(previewCount(userList, emptyDiary.getId())).isZero();
            softly.assertThat(previewCount(myList, diary.getId())).isEqualTo(4);
            softly.assertThat(previewCount(myList, emptyDiary.getId())).isZero();
        });
    }

    private CommentPreviewResponse previewOf(UserEntity asUser, DiaryEntity target) throws Exception {
        loginAs(asUser);
        JsonNode node = objectMapper.readTree(mockMvc.perform(get("/api/diaries/{diaryId}", target.getId())
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        return objectMapper.treeToValue(node.path("commentPreview"), CommentPreviewResponse.class);
    }

    private long previewCount(JsonNode page, Long diaryId) {
        for (JsonNode item : page.path("content")) {
            if (item.path("diaryId").asLong() == diaryId) {
                return item.path("commentPreview").path("commentCount").asLong();
            }
        }
        throw new AssertionError("diary not in page: " + diaryId);
    }

    private void loginAs(UserEntity user) {
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());
    }
}
