package apptive.team5.diary.repository;

import apptive.team5.diary.domain.DiaryReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryReportRepository extends JpaRepository<DiaryReportEntity, Long> {
}
