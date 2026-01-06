package apptive.team5.diary.repository;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryReportEntity;
import apptive.team5.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DiaryReportRepository extends JpaRepository<DiaryReportEntity, Long> {

    @Modifying(clearAutomatically = true)
    @Query("delete from DiaryReportEntity dr where dr.diary.id = :diaryId")
    void deleteByDiaryId(Long diaryId);

    @Modifying(clearAutomatically = true)
    @Query("delete from DiaryReportEntity dr where dr.diary.id in :diaryIds")
    void deleteByDiaryIds(List<Long> diaryIds);

    List<DiaryReportEntity> findByDiaryId(Long diaryId);

    @Modifying(clearAutomatically = true)
    @Query("delete from DiaryReportEntity dr")
    void deleteAllWithBulk();

    @Modifying(clearAutomatically = true)
    @Query("delete from DiaryReportEntity dr where dr.user.id = :userId")
    void deleteByUserId(Long userId);

    boolean existsByUserAndDiary(UserEntity user, DiaryEntity diary);
}
