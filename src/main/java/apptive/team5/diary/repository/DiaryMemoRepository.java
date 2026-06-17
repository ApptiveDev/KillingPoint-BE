package apptive.team5.diary.repository;

import apptive.team5.diary.domain.DiaryMemoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DiaryMemoRepository extends JpaRepository<DiaryMemoEntity, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from DiaryMemoEntity dm where dm.diary.id = :diaryId")
    void deleteByDiaryId(Long diaryId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from DiaryMemoEntity dm where dm.diary.id in :diaryIds")
    void deleteByDiaryIds(List<Long> diaryIds);
}
