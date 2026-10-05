package apptive.team5.comment.controller;

import apptive.team5.comment.domain.CommentEntity;
import apptive.team5.comment.domain.CommentStatus;
import apptive.team5.comment.dto.CommentCreateRequest;
import apptive.team5.comment.dto.CommentResponse;
import apptive.team5.comment.dto.CommentUpdateRequest;
import apptive.team5.comment.repository.CommentRepository;
import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryScope;
import apptive.team5.diary.repository.DiaryRepository;
import apptive.team5.user.domain.UserBlock;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.repository.UserBlockRepository;
import apptive.team5.user.repository.UserRepository;
import apptive.team5.util.TestSecurityContextHolderInjection;
import apptive.team5.util.TestUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentControllerTest {

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
    private UserEntity currentUser;
    private DiaryEntity diary;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(TestUtil.makeUserEntity());
        viewer = userRepository.save(TestUtil.makeDifferentUserEntity(owner));
        diary = diaryRepository.save(TestUtil.makeDiaryEntity(owner));

        loginAs(viewer);
    }

    @Test
    @DisplayName("댓글 작성")
    void createComment() throws Exception {
        CommentCreateRequest request = new CommentCreateRequest("첫 댓글", null);

        String body = mockMvc.perform(post("/api/diaries/{diaryId}/comments", diary.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getContentAsString();

        CommentResponse response = objectMapper.readValue(body, CommentResponse.class);

        assertSoftly(softly -> {
            softly.assertThat(response.text()).isEqualTo("첫 댓글");
            softly.assertThat(response.status()).isEqualTo(CommentStatus.ACTIVE);
            softly.assertThat(response.parentCommentId()).isNull();
            softly.assertThat(response.author().userId()).isEqualTo(viewer.getId());
            softly.assertThat(response.isMine()).isTrue();
            softly.assertThat(response.isEdited()).isFalse();
            softly.assertThat(commentRepository.findById(response.commentId())).isPresent();
        });
    }

    @Test
    @DisplayName("답글 작성 시 원댓글의 replyCount 가 증가한다")
    void createReply() throws Exception {
        CommentEntity parent = commentRepository.save(TestUtil.makeCommentEntity(diary, owner, "원댓글"));
        CommentCreateRequest request = new CommentCreateRequest("답글", parent.getId());

        String body = mockMvc.perform(post("/api/diaries/{diaryId}/comments", diary.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        CommentResponse reply = objectMapper.readValue(body, CommentResponse.class);
        List<CommentResponse> comments = getComments();

        assertSoftly(softly -> {
            softly.assertThat(reply.parentCommentId()).isEqualTo(parent.getId());
            softly.assertThat(comments).hasSize(1);
            softly.assertThat(comments.getFirst().replyCount()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("답글의 답글은 작성할 수 없다")
    void createReplyToReplyFails() throws Exception {
        CommentEntity parent = commentRepository.save(TestUtil.makeCommentEntity(diary, owner, "원댓글"));
        CommentEntity reply = commentRepository.save(TestUtil.makeReplyEntity(diary, owner, parent, "답글"));
        CommentCreateRequest request = new CommentCreateRequest("답글의 답글", reply.getId());

        String body = mockMvc.perform(post("/api/diaries/{diaryId}/comments", diary.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertThat(objectMapper.readTree(body).path("message").asText()).isEqualTo("답글을 달 수 없는 댓글입니다.");
    }

    @Test
    @DisplayName("삭제된 원댓글에는 답글을 달 수 없다")
    void createReplyToDeletedParentFails() throws Exception {
        CommentEntity parent = commentRepository.save(TestUtil.makeCommentEntity(diary, owner, "원댓글"));
        parent.delete();
        CommentCreateRequest request = new CommentCreateRequest("답글", parent.getId());

        mockMvc.perform(post("/api/diaries/{diaryId}/comments", diary.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("다른 킬링파트의 댓글에는 답글을 달 수 없다")
    void createReplyToOtherDiaryCommentFails() throws Exception {
        DiaryEntity otherDiary = diaryRepository.save(TestUtil.makeDiaryEntity(owner));
        CommentEntity parent = commentRepository.save(TestUtil.makeCommentEntity(otherDiary, owner, "다른 다이어리 댓글"));
        CommentCreateRequest request = new CommentCreateRequest("답글", parent.getId());

        mockMvc.perform(post("/api/diaries/{diaryId}/comments", diary.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PRIVATE 킬링파트에는 소유자만 댓글을 달 수 있다")
    void createCommentOnPrivateDiaryFails() throws Exception {
        DiaryEntity privateDiary = diaryRepository.save(TestUtil.makeDiaryEntityWithScope(owner, DiaryScope.PRIVATE));
        CommentCreateRequest request = new CommentCreateRequest("몰래 댓글", null);

        mockMvc.perform(post("/api/diaries/{diaryId}/comments", privateDiary.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        loginAs(owner);

        mockMvc.perform(post("/api/diaries/{diaryId}/comments", privateDiary.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("공백 댓글은 작성할 수 없다")
    void createBlankCommentFails() throws Exception {
        CommentCreateRequest request = new CommentCreateRequest("   ", null);

        mockMvc.perform(post("/api/diaries/{diaryId}/comments", diary.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 목록 - 좋아요가 같으면 최신 댓글이 먼저")
    void getCommentsNewerFirstWhenSameLikes() throws Exception {
        CommentEntity older = commentRepository.save(TestUtil.makeCommentEntity(diary, owner, "1시간 전"));
        CommentEntity newer = commentRepository.save(TestUtil.makeCommentEntity(diary, owner, "방금"));
        setCreatedAt(older, LocalDateTime.now().minusHours(1));
        flushAndClear();

        List<Long> ids = getComments().stream().map(CommentResponse::commentId).toList();

        assertThat(ids).containsExactly(newer.getId(), older.getId());
    }

    @Test
    @DisplayName("댓글 목록 - 1시간 전 좋아요 3개가 방금 쓴 좋아요 0개보다 앞선다")
    void getCommentsLikesBeatRecency() throws Exception {
        CommentEntity liked = commentRepository.save(TestUtil.makeCommentEntity(diary, owner, "1시간 전, 좋아요 3"));
        CommentEntity fresh = commentRepository.save(TestUtil.makeCommentEntity(diary, owner, "방금, 좋아요 0"));
        setCreatedAt(liked, LocalDateTime.now().minusHours(1));
        setLikeCount(liked, 3);
        flushAndClear();

        List<Long> ids = getComments().stream().map(CommentResponse::commentId).toList();

        assertThat(ids).containsExactly(liked.getId(), fresh.getId());
    }

    @Test
    @DisplayName("댓글 목록 - 10일 전 좋아요 100개는 방금 쓴 좋아요 0개에 밀린다")
    void getCommentsOldPopularLosesToFresh() throws Exception {
        CommentEntity oldPopular = commentRepository.save(TestUtil.makeCommentEntity(diary, owner, "10일 전, 좋아요 100"));
        CommentEntity fresh = commentRepository.save(TestUtil.makeCommentEntity(diary, owner, "방금, 좋아요 0"));
        setCreatedAt(oldPopular, LocalDateTime.now().minusDays(10));
        setLikeCount(oldPopular, 100);
        flushAndClear();

        List<Long> ids = getComments().stream().map(CommentResponse::commentId).toList();

        assertThat(ids).containsExactly(fresh.getId(), oldPopular.getId());
    }

    @Test
    @DisplayName("댓글 목록 - 페이지를 이어 붙이면 전체와 같다")
    void getCommentsPaging() throws Exception {
        for (int i = 0; i < 5; i++) {
            commentRepository.save(TestUtil.makeCommentEntity(diary, owner, "댓글 " + i));
        }
        flushAndClear();

        JsonNode page0 = getCommentsNode(0, 2);
        JsonNode page1 = getCommentsNode(1, 2);
        JsonNode page2 = getCommentsNode(2, 2);

        List<Long> collected = new java.util.ArrayList<>();
        for (JsonNode page : List.of(page0, page1, page2)) {
            page.path("comments").path("content").forEach(node -> collected.add(node.path("commentId").asLong()));
        }

        assertSoftly(softly -> {
            softly.assertThat(page0.path("comments").path("page").path("totalElements").asLong()).isEqualTo(5);
            softly.assertThat(page0.path("commentCount").asLong()).isEqualTo(5);
            softly.assertThat(collected).hasSize(5).doesNotHaveDuplicates();
        });
    }

    @Test
    @DisplayName("댓글 목록 - 답글 없는 삭제 댓글은 숨기고, 답글 있는 삭제 댓글은 플레이스홀더로 남긴다")
    void getCommentsDeletedPlaceholder() throws Exception {
        CommentEntity deletedAlone = commentRepository.save(TestUtil.makeCommentEntity(diary, owner, "혼자 삭제"));
        CommentEntity deletedWithReply = commentRepository.save(TestUtil.makeCommentEntity(diary, owner, "답글 있는 삭제"));
        commentRepository.save(TestUtil.makeReplyEntity(diary, viewer, deletedWithReply, "살아있는 답글"));
        CommentEntity active = commentRepository.save(TestUtil.makeCommentEntity(diary, owner, "정상"));
        deletedAlone.delete();
        deletedWithReply.delete();
        flushAndClear();

        JsonNode node = getCommentsNode(0, 20);
        List<CommentResponse> comments = toComments(node);
        CommentResponse placeholder = comments.stream()
                .filter(c -> c.commentId().equals(deletedWithReply.getId()))
                .findFirst().orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(node.path("commentCount").asLong()).isEqualTo(1);
            softly.assertThat(comments).extracting(CommentResponse::commentId)
                    .containsExactlyInAnyOrder(deletedWithReply.getId(), active.getId());
            softly.assertThat(placeholder.status()).isEqualTo(CommentStatus.DELETED);
            softly.assertThat(placeholder.text()).isNull();
            softly.assertThat(placeholder.author()).isNull();
            softly.assertThat(placeholder.replyCount()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("댓글 목록 - 차단 관계인 유저의 댓글은 양방향으로 마스킹된다")
    void getCommentsBlockedMasked() throws Exception {
        UserEntity blockedByViewer = userRepository.save(TestUtil.makeDifferentUserEntity(viewer));
        UserEntity blockingViewer = userRepository.save(TestUtil.makeDifferentUserEntity(blockedByViewer));
        userBlockRepository.save(new UserBlock(viewer, blockedByViewer));
        userBlockRepository.save(new UserBlock(blockingViewer, viewer));

        CommentEntity byBlocked = commentRepository.save(TestUtil.makeCommentEntity(diary, blockedByViewer, "내가 차단한 사람"));
        CommentEntity byBlocking = commentRepository.save(TestUtil.makeCommentEntity(diary, blockingViewer, "나를 차단한 사람"));
        CommentEntity byOwner = commentRepository.save(TestUtil.makeCommentEntity(diary, owner, "정상"));
        flushAndClear();

        List<CommentResponse> comments = getComments();
        CommentResponse masked1 = find(comments, byBlocked.getId());
        CommentResponse masked2 = find(comments, byBlocking.getId());
        CommentResponse normal = find(comments, byOwner.getId());

        assertSoftly(softly -> {
            softly.assertThat(masked1.isBlocked()).isTrue();
            softly.assertThat(masked1.text()).isNull();
            softly.assertThat(masked1.author().userId()).isEqualTo(blockedByViewer.getId());
            softly.assertThat(masked1.author().username()).isNull();
            softly.assertThat(masked2.isBlocked()).isTrue();
            softly.assertThat(normal.isBlocked()).isFalse();
            softly.assertThat(normal.text()).isEqualTo("정상");
        });
    }

    @Test
    @DisplayName("답글 목록은 오래된 순이고 삭제된 답글은 제외된다")
    void getReplies() throws Exception {
        CommentEntity parent = commentRepository.save(TestUtil.makeCommentEntity(diary, owner, "원댓글"));
        CommentEntity first = commentRepository.save(TestUtil.makeReplyEntity(diary, viewer, parent, "첫 답글"));
        CommentEntity deleted = commentRepository.save(TestUtil.makeReplyEntity(diary, viewer, parent, "삭제될 답글"));
        CommentEntity last = commentRepository.save(TestUtil.makeReplyEntity(diary, owner, parent, "마지막 답글"));
        deleted.delete();
        flushAndClear();

        String body = mockMvc.perform(get("/api/comments/{commentId}/replies", parent.getId())
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CommentResponse> replies = objectMapper.convertValue(
                objectMapper.readTree(body).path("content"),
                new TypeReference<List<CommentResponse>>() {}
        );

        assertThat(replies).extracting(CommentResponse::commentId)
                .containsExactly(first.getId(), last.getId());
    }

    @Test
    @DisplayName("댓글 수정")
    void updateComment() throws Exception {
        CommentEntity comment = commentRepository.save(TestUtil.makeCommentEntity(diary, viewer, "수정 전"));
        CommentUpdateRequest request = new CommentUpdateRequest("수정 후");

        String body = mockMvc.perform(put("/api/comments/{commentId}", comment.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentResponse response = objectMapper.readValue(body, CommentResponse.class);

        assertSoftly(softly -> {
            softly.assertThat(response.text()).isEqualTo("수정 후");
            softly.assertThat(response.isEdited()).isTrue();
            softly.assertThat(commentRepository.findById(comment.getId()).orElseThrow().getContent()).isEqualTo("수정 후");
        });
    }

    @Test
    @DisplayName("남의 댓글은 수정할 수 없다")
    void updateOthersCommentFails() throws Exception {
        CommentEntity comment = commentRepository.save(TestUtil.makeCommentEntity(diary, owner, "남의 댓글"));
        CommentUpdateRequest request = new CommentUpdateRequest("훔쳐 수정");

        mockMvc.perform(put("/api/comments/{commentId}", comment.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("삭제된 댓글은 수정할 수 없다")
    void updateDeletedCommentFails() throws Exception {
        CommentEntity comment = commentRepository.save(TestUtil.makeCommentEntity(diary, viewer, "삭제됨"));
        comment.delete();
        CommentUpdateRequest request = new CommentUpdateRequest("되살리기");

        mockMvc.perform(put("/api/comments/{commentId}", comment.getId())
                        .with(securityContext(SecurityContextHolder.getContext()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 삭제는 soft delete 로 처리된다")
    void deleteComment() throws Exception {
        CommentEntity comment = commentRepository.save(TestUtil.makeCommentEntity(diary, viewer, "지울 댓글"));

        mockMvc.perform(delete("/api/comments/{commentId}", comment.getId())
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isNoContent());

        CommentEntity found = commentRepository.findById(comment.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(CommentStatus.DELETED);
    }

    @Test
    @DisplayName("남의 댓글은 삭제할 수 없다")
    void deleteOthersCommentFails() throws Exception {
        CommentEntity comment = commentRepository.save(TestUtil.makeCommentEntity(diary, owner, "남의 댓글"));

        mockMvc.perform(delete("/api/comments/{commentId}", comment.getId())
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제는 404")
    void deleteNotFoundComment() throws Exception {
        String body = mockMvc.perform(delete("/api/comments/{commentId}", 999_999L)
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        assertThat(objectMapper.readTree(body).path("message").asText()).isEqualTo("존재하지 않는 댓글입니다.");
    }

    private void loginAs(UserEntity user) {
        currentUser = user;
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());
    }

    private List<CommentResponse> getComments() throws Exception {
        return toComments(getCommentsNode(0, 20));
    }

    private JsonNode getCommentsNode(int page, int size) throws Exception {
        loginAs(currentUser);
        String body = mockMvc.perform(get("/api/diaries/{diaryId}/comments", diary.getId())
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body);
    }

    private List<CommentResponse> toComments(JsonNode node) {
        return objectMapper.convertValue(
                node.path("comments").path("content"),
                new TypeReference<List<CommentResponse>>() {}
        );
    }

    private CommentResponse find(List<CommentResponse> comments, Long commentId) {
        return comments.stream().filter(c -> c.commentId().equals(commentId)).findFirst().orElseThrow();
    }

    private void setCreatedAt(CommentEntity comment, LocalDateTime createdAt) {
        em.createQuery("update CommentEntity c set c.createDateTime = :t where c.id = :id")
                .setParameter("t", createdAt)
                .setParameter("id", comment.getId())
                .executeUpdate();
    }

    private void setLikeCount(CommentEntity comment, long likeCount) {
        em.createQuery("update CommentEntity c set c.likeCount = :l where c.id = :id")
                .setParameter("l", likeCount)
                .setParameter("id", comment.getId())
                .executeUpdate();
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
