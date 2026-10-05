package apptive.team5.comment.repository;

import apptive.team5.comment.domain.CommentLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface CommentLikeRepository extends JpaRepository<CommentLikeEntity, Long> {

    @Query("""
            select cl.comment.id
            from CommentLikeEntity cl
            where cl.user.id = :userId and cl.comment.id in :commentIds
            """)
    Set<Long> findLikedCommentIdsByUser(@Param("userId") Long userId, @Param("commentIds") List<Long> commentIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CommentLikeEntity cl where cl.comment.id in :commentIds")
    void deleteByCommentIds(@Param("commentIds") List<Long> commentIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CommentLikeEntity cl where cl.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
