package apptive.team5.comment.repository;

import apptive.team5.comment.domain.CommentEntity;
import apptive.team5.comment.domain.CommentReportEntity;
import apptive.team5.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentReportRepository extends JpaRepository<CommentReportEntity, Long> {

    boolean existsByUserAndComment(UserEntity user, CommentEntity comment);

    List<CommentReportEntity> findByCommentId(Long commentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CommentReportEntity cr where cr.comment.id in :commentIds")
    void deleteByCommentIds(@Param("commentIds") List<Long> commentIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CommentReportEntity cr where cr.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
