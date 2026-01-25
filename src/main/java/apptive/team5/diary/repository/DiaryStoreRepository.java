package apptive.team5.diary.repository;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryStoreEntity;
import apptive.team5.user.domain.UserEntity;
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
            SELECT dl.diary.id
            FROM DiaryLikeEntity dl
            WHERE dl.user.id = :userId
            AND dl.diary.id IN :diaryIds
    """)
    Set<Long> findStoredDiaryIdsByUser(
            @Param("userId")
            Long userId,
            @Param("diaryIds")
            List<Long> diaryIds
    );

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

    List<DiaryStoreEntity> Diary(DiaryEntity diary);
}
