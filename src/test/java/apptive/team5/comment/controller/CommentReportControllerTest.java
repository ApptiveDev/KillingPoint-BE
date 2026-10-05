package apptive.team5.comment.controller;

import apptive.team5.comment.domain.CommentEntity;
import apptive.team5.comment.domain.CommentReportEntity;
import apptive.team5.comment.dto.CommentReportRequestDto;
import apptive.team5.comment.repository.CommentReportRepository;
import apptive.team5.comment.repository.CommentRepository;
import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.repository.DiaryRepository;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.repository.UserRepository;
import apptive.team5.util.TestSecurityContextHolderInjection;
import apptive.team5.util.TestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentReportControllerTest {

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
    private CommentReportRepository commentReportRepository;

    private UserEntity reporter;
    private UserEntity author;
    private CommentEntity comment;

    @BeforeEach
    void setUp() {
        reporter = userRepository.save(TestUtil.makeUserEntity());
        author = userRepository.save(TestUtil.makeDifferentUserEntity(reporter));
        DiaryEntity diary = diaryRepository.save(TestUtil.makeDiaryEntity(author));
        comment = commentRepository.save(TestUtil.makeCommentEntity(diary, author, "나쁜 댓글"));
        TestSecurityContextHolderInjection.inject(reporter.getId(), reporter.getRoleType());
    }

    @Test
    @DisplayName("댓글 신고 성공 - 신고 시점의 본문이 스냅샷으로 남는다")
    void reportCommentSuccess() throws Exception {
        report(comment.getId(), new CommentReportRequestDto("욕설이에요"))
                .andExpect(status().isCreated());

        List<CommentReportEntity> reports = commentReportRepository.findByCommentId(comment.getId());

        assertSoftly(softly -> {
            softly.assertThat(reports).hasSize(1);
            softly.assertThat(reports.getFirst().getReason()).isEqualTo("욕설이에요");
            softly.assertThat(reports.getFirst().getReportContent()).isEqualTo("나쁜 댓글");
            softly.assertThat(reports.getFirst().getUser().getId()).isEqualTo(reporter.getId());
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "    "})
    @DisplayName("댓글 신고 실패 - 신고 내용은 1자 이상 200자 이하")
    void reportCommentFailDueToContentLimit(String content) throws Exception {
        report(comment.getId(), new CommentReportRequestDto(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 신고 실패 - 이미 신고한 댓글")
    void reportCommentFailDueToDuplicate() throws Exception {
        commentReportRepository.save(new CommentReportEntity("먼저 신고", comment.getContent(), comment, reporter));

        String body = report(comment.getId(), new CommentReportRequestDto("또 신고"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(objectMapper.readTree(body).path("message").asText())
                .isEqualTo(ExceptionCode.DUPLICATE_COMMENT_REPORT.getDescription());
    }

    @Test
    @DisplayName("댓글 신고 실패 - 삭제된 댓글")
    void reportDeletedCommentFails() throws Exception {
        comment.delete();
        em.flush();
        em.clear();

        report(comment.getId(), new CommentReportRequestDto("신고"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 신고 실패 - 존재하지 않는 댓글")
    void reportNotFoundComment() throws Exception {
        report(999_999L, new CommentReportRequestDto("신고"))
                .andExpect(status().isNotFound());
    }

    private org.springframework.test.web.servlet.ResultActions report(Long commentId, CommentReportRequestDto request) throws Exception {
        return mockMvc.perform(post("/api/comments/{commentId}/reports", commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(securityContext(SecurityContextHolder.getContext())));
    }
}
