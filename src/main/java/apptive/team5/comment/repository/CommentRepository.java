package apptive.team5.comment.repository;

import apptive.team5.comment.domain.CommentEntity;
import apptive.team5.comment.domain.CommentStatus;
import apptive.team5.comment.dto.CommentReplyCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    // 점수 = (1 + log2(1 + 좋아요)) / (1 + 경과시간(h) / 24). LOG2, TIMESTAMPDIFF 가 JPQL 에 없어 native 로 작성.
    @Query(value = """
            SELECT c.comment_id
            FROM diary_comment c
            WHERE c.diary_id = :diaryId
              AND c.parent_id IS NULL
              AND (c.status = 'ACTIVE'
                   OR EXISTS (SELECT 1 FROM diary_comment r
                              WHERE r.parent_id = c.comment_id AND r.status = 'ACTIVE'))
            ORDER BY (1 + LOG2(1 + c.likeCount)) / (1 + TIMESTAMPDIFF(HOUR, c.createDateTime, NOW()) / 24.0) DESC,
                     c.comment_id DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM diary_comment c
            WHERE c.diary_id = :diaryId
              AND c.parent_id IS NULL
              AND (c.status = 'ACTIVE'
                   OR EXISTS (SELECT 1 FROM diary_comment r
                              WHERE r.parent_id = c.comment_id AND r.status = 'ACTIVE'))
            """,
            nativeQuery = true)
    Page<Long> findTopLevelIdsOrderByPopularity(@Param("diaryId") Long diaryId, Pageable pageable);

    @Query("select c from CommentEntity c left join fetch c.user where c.id in :ids")
    List<CommentEntity> findAllByIdInWithUser(@Param("ids") List<Long> ids);

    @Query("select c from CommentEntity c left join fetch c.user left join fetch c.diary where c.id = :commentId")
    Optional<CommentEntity> findByIdWithUserAndDiary(@Param("commentId") Long commentId);

    @Query(value = """
            select c from CommentEntity c
            left join fetch c.user
            where c.parent.id = :parentId and c.status = :status
            order by c.id asc
            """,
            countQuery = """
            select count(c) from CommentEntity c
            where c.parent.id = :parentId and c.status = :status
            """)
    Page<CommentEntity> findRepliesByParentId(
            @Param("parentId") Long parentId,
            @Param("status") CommentStatus status,
            Pageable pageable
    );

    @Query("""
            select new apptive.team5.comment.dto.CommentReplyCountDto(c.parent.id, count(c))
            from CommentEntity c
            where c.parent.id in :parentIds and c.status = :status
            group by c.parent.id
            """)
    List<CommentReplyCountDto> countRepliesByParentIds(
            @Param("parentIds") List<Long> parentIds,
            @Param("status") CommentStatus status
    );

    @Query("""
            select count(c) from CommentEntity c
            where c.diary.id = :diaryId and c.parent is null and c.status = :status
            """)
    long countTopLevelByDiaryId(@Param("diaryId") Long diaryId, @Param("status") CommentStatus status);

    @Query("select c.id from CommentEntity c where c.diary.id in :diaryIds")
    List<Long> findIdsByDiaryIds(@Param("diaryIds") List<Long> diaryIds);

    @Query("select c.id from CommentEntity c where c.user.id = :userId")
    List<Long> findIdsByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update CommentEntity c set c.likeCount = c.likeCount + 1 where c.id = :commentId")
    void increaseLikeCount(@Param("commentId") Long commentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update CommentEntity c set c.likeCount = c.likeCount - 1 where c.id = :commentId and c.likeCount > 0")
    void decreaseLikeCount(@Param("commentId") Long commentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CommentEntity c where c.diary.id in :diaryIds and c.parent is not null")
    void deleteRepliesByDiaryIds(@Param("diaryIds") List<Long> diaryIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CommentEntity c where c.diary.id in :diaryIds and c.parent is null")
    void deleteTopLevelByDiaryIds(@Param("diaryIds") List<Long> diaryIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update CommentEntity c set c.likeCount = c.likeCount - 1
            where c.likeCount > 0
              and c.id in (select cl.comment.id from CommentLikeEntity cl where cl.user.id = :userId)
            """)
    void decrementLikeCountForUserLikes(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update CommentEntity c set c.status = :status, c.user = null where c.user.id = :userId")
    void markDeletedAndDetachUser(@Param("userId") Long userId, @Param("status") CommentStatus status);
}
