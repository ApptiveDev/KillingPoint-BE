package apptive.team5.diary.repository;

import apptive.team5.diary.domain.DiaryReportEntity;
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
    void deleteByDiaryIds(List<Long> diaryId);

    List<DiaryReportEntity> findByDiaryId(Long diaryId);
}
