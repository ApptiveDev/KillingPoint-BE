package apptive.team5.alarmv2.repository;

import apptive.team5.alarmv2.domain.AlarmDispatchV2;
import apptive.team5.alarmv2.domain.AlarmDispatchV2Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import java.util.List;
import java.time.LocalDateTime;
import jakarta.persistence.LockModeType;

public interface AlarmDispatchV2Repository extends JpaRepository<AlarmDispatchV2, Long> {

    long countByStatus(AlarmDispatchV2Status status);

    List<AlarmDispatchV2> findTop20ByOrderByCreateDateTimeDesc(Pageable pageable);

    List<AlarmDispatchV2> findByStatusOrderByScheduledAtAsc(AlarmDispatchV2Status status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<AlarmDispatchV2> findTop20ByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(
            AlarmDispatchV2Status status, LocalDateTime scheduledAt);
}
