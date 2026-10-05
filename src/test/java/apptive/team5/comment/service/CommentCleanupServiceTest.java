package apptive.team5.comment.service;

import apptive.team5.comment.domain.CommentEntity;
import apptive.team5.comment.domain.CommentLikeEntity;
import apptive.team5.comment.domain.CommentStatus;
import apptive.team5.comment.repository.CommentLikeRepository;
import apptive.team5.comment.repository.CommentRepository;
import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.repository.DiaryRepository;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.repository.UserRepository;
import apptive.team5.util.TestUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@SpringBootTest
@Transactional
class CommentCleanupServiceTest {

    @Autowired
    private CommentCleanupService commentCleanupService;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private CommentLikeRepository commentLikeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DiaryRepository diaryRepository;
    @Autowired
    private EntityManager em;

    private UserEntity owner;
    private UserEntity other;
    private DiaryEntity diary;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(TestUtil.makeUserEntity());
        other = userRepository.save(TestUtil.makeDifferentUserEntity(owner));
        diary = diaryRepository.save(TestUtil.makeDiaryEntity(owner));
    }

    @Test
    @DisplayName("다이어리 삭제 시 댓글, 답글, 좋아요가 모두 지워진다")
    void deleteByDiaryIds() {
        CommentEntity parent = commentRepository.save(TestUtil.makeCommentEntity(diary, other, "원댓글"));
        CommentEntity reply = commentRepository.save(TestUtil.makeReplyEntity(diary, owner, parent, "답글"));
        commentLikeRepository.save(new CommentLikeEntity(owner, parent));
        em.flush();
        em.clear();

        commentCleanupService.deleteByDiaryIds(List.of(diary.getId()));

        assertSoftly(softly -> {
            softly.assertThat(commentRepository.findById(parent.getId())).isEmpty();
            softly.assertThat(commentRepository.findById(reply.getId())).isEmpty();
            softly.assertThat(commentLikeRepository.findAll()).isEmpty();
        });
    }

    @Test
    @DisplayName("회원 탈퇴 시 본인 댓글은 DELETED + 작성자 해제되고 남의 답글은 남는다")
    void handleUserWithdrawal() {
        CommentEntity othersParent = commentRepository.save(TestUtil.makeCommentEntity(diary, other, "탈퇴자 원댓글"));
        CommentEntity ownersReply = commentRepository.save(TestUtil.makeReplyEntity(diary, owner, othersParent, "남이 단 답글"));
        CommentEntity ownersComment = commentRepository.save(TestUtil.makeCommentEntity(diary, owner, "남의 댓글"));
        commentLikeRepository.save(new CommentLikeEntity(other, ownersComment));
        em.createQuery("update CommentEntity c set c.likeCount = 1 where c.id = :id")
                .setParameter("id", ownersComment.getId())
                .executeUpdate();
        em.flush();
        em.clear();

        commentCleanupService.handleUserWithdrawal(other.getId());
        em.flush();
        em.clear();

        CommentEntity foundParent = commentRepository.findById(othersParent.getId()).orElseThrow();
        CommentEntity foundReply = commentRepository.findById(ownersReply.getId()).orElseThrow();
        CommentEntity foundOwnersComment = commentRepository.findById(ownersComment.getId()).orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(foundParent.getStatus()).isEqualTo(CommentStatus.DELETED);
            softly.assertThat(foundParent.getUser()).isNull();
            softly.assertThat(foundReply.getStatus()).isEqualTo(CommentStatus.ACTIVE);
            softly.assertThat(foundReply.getParent().getId()).isEqualTo(othersParent.getId());
            softly.assertThat(foundOwnersComment.getLikeCount()).isEqualTo(0);
            softly.assertThat(commentLikeRepository.findAll()).isEmpty();
        });
    }
}
