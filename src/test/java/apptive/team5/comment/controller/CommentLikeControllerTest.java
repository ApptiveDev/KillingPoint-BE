package apptive.team5.comment.controller;

import apptive.team5.comment.domain.CommentEntity;
import apptive.team5.comment.domain.CommentLikeEntity;
import apptive.team5.comment.dto.CommentLikeResponse;
import apptive.team5.comment.dto.CommentResponse;
import apptive.team5.comment.repository.CommentLikeRepository;
import apptive.team5.comment.repository.CommentRepository;
import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.repository.DiaryRepository;
import apptive.team5.user.domain.UserEntity;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentLikeControllerTest {

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
    private CommentLikeRepository commentLikeRepository;

    private UserEntity owner;
    private UserEntity liker;
    private DiaryEntity diary;
    private CommentEntity comment;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(TestUtil.makeUserEntity());
        liker = userRepository.save(TestUtil.makeDifferentUserEntity(owner));
        diary = diaryRepository.save(TestUtil.makeDiaryEntity(owner));
        comment = commentRepository.save(TestUtil.makeCommentEntity(diary, owner, "댓글"));
        loginAs(liker);
    }

    @Test
    @DisplayName("좋아요 추가")
    void toggleLikeAdd() throws Exception {
        CommentLikeResponse response = toggle(comment.getId());

        assertSoftly(softly -> {
            softly.assertThat(response.isLiked()).isTrue();
            softly.assertThat(response.likeCount()).isEqualTo(1);
            softly.assertThat(commentLikeRepository.existsByUserAndComment(liker, comment)).isTrue();
            softly.assertThat(commentRepository.findById(comment.getId()).orElseThrow().getLikeCount()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("좋아요 취소")
    void toggleLikeRemove() throws Exception {
        commentLikeRepository.save(new CommentLikeEntity(liker, comment));
        em.createQuery("update CommentEntity c set c.likeCount = 1 where c.id = :id")
                .setParameter("id", comment.getId())
                .executeUpdate();
        em.flush();
        em.clear();

        CommentLikeResponse response = toggle(comment.getId());

        assertSoftly(softly -> {
            softly.assertThat(response.isLiked()).isFalse();
            softly.assertThat(response.likeCount()).isEqualTo(0);
            softly.assertThat(commentLikeRepository.findAll()).isEmpty();
        });
    }

    @Test
    @DisplayName("두 번 누르면 원래대로 돌아온다")
    void toggleTwice() throws Exception {
        toggle(comment.getId());
        loginAs(liker);
        CommentLikeResponse response = toggle(comment.getId());

        assertSoftly(softly -> {
            softly.assertThat(response.isLiked()).isFalse();
            softly.assertThat(response.likeCount()).isEqualTo(0);
        });
    }

    @Test
    @DisplayName("좋아요 후 목록에 isLiked 와 likeCount 가 반영된다")
    void likeReflectedInList() throws Exception {
        toggle(comment.getId());
        loginAs(liker);

        String body = mockMvc.perform(get("/api/diaries/{diaryId}/comments", diary.getId())
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CommentResponse> comments = objectMapper.convertValue(
                objectMapper.readTree(body).path("comments").path("content"),
                new TypeReference<List<CommentResponse>>() {}
        );

        assertSoftly(softly -> {
            softly.assertThat(comments).hasSize(1);
            softly.assertThat(comments.getFirst().isLiked()).isTrue();
            softly.assertThat(comments.getFirst().likeCount()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("삭제된 댓글에는 좋아요를 누를 수 없다")
    void toggleLikeOnDeletedCommentFails() throws Exception {
        comment.delete();
        em.flush();
        em.clear();

        String body = mockMvc.perform(post("/api/comments/{commentId}/like", comment.getId())
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertThat(objectMapper.readTree(body).path("message").asText()).isEqualTo("삭제된 댓글입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 댓글 좋아요는 404")
    void toggleLikeNotFound() throws Exception {
        mockMvc.perform(post("/api/comments/{commentId}/like", 999_999L)
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isNotFound());
    }

    private CommentLikeResponse toggle(Long commentId) throws Exception {
        String body = mockMvc.perform(post("/api/comments/{commentId}/like", commentId)
                        .with(securityContext(SecurityContextHolder.getContext())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(body, CommentLikeResponse.class);
    }

    private void loginAs(UserEntity user) {
        TestSecurityContextHolderInjection.inject(user.getId(), user.getRoleType());
    }
}
