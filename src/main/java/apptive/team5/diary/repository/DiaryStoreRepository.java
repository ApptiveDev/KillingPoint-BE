package apptive.team5.diary.repository;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryStoreEntity;
import apptive.team5.user.domain.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface DiaryStoreRepository extends JpaRepository<DiaryStoreEntity, Long> {

    boolean existsByUserAndDiary(UserEntity user, DiaryEntity diary);

    DiaryStoreEntity findByUserAndDiary(UserEntity user, DiaryEntity diary);

    @Query("""
            SELECT ds.diary.id
            FROM DiaryStoreEntity ds
            WHERE ds.user.id = :userId
            AND ds.diary.id IN :diaryIds
    """)
    Set<Long> findStoredDiaryIdsByUser(
            @Param("userId")
            Long userId,
            @Param("diaryIds")
            List<Long> diaryIds
    );

    @Query(value = """
            SELECT ds
            FROM DiaryStoreEntity ds
            JOIN FETCH ds.diary
            WHERE ds.user.id = :userId
            ORDER BY ds.id desc
    """,
            countQuery = """
            SELECT count(ds.id)
            FROM DiaryStoreEntity ds
            WHERE ds.user.id = :userId
    """)
    Page<DiaryStoreEntity> findStoredDiaryByUserWithDiary(Long userId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM DiaryStoreEntity ds
            WHERE ds.user.id = :userId
    """)
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM DiaryStoreEntity ds
            WHERE ds.diary.id = :diaryId
    """)
    void deleteByDiaryId(@Param("diaryId") Long diaryId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM DiaryStoreEntity ds
            WHERE ds.diary.id in :diaryIds
     """)
    void deleteByDiaryIds(@Param("diaryIds") List<Long> diaryIds);

}
