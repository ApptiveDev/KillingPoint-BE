package apptive.team5.alarmv2.repository;

import apptive.team5.alarmv2.domain.AdminNotice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminNoticeRepository extends JpaRepository<AdminNotice, Long> {
    List<AdminNotice> findTop20ByOrderByCreateDateTimeDesc(Pageable pageable);
}
