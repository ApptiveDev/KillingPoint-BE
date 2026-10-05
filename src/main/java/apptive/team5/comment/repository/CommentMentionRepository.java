package apptive.team5.comment.repository;

import apptive.team5.comment.domain.CommentMentionEntity;
import apptive.team5.comment.domain.MentionTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentMentionRepository extends JpaRepository<CommentMentionEntity, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CommentMentionEntity m where m.comment.id in :commentIds")
    void deleteByCommentIds(@Param("commentIds") List<Long> commentIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CommentMentionEntity m where m.targetType = :targetType and m.targetId = :targetId")
    void deleteByTarget(@Param("targetType") MentionTargetType targetType, @Param("targetId") Long targetId);
}
