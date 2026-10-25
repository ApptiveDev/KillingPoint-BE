package apptive.team5.alarmv2.repository;

import apptive.team5.alarmv2.domain.AlarmDispatchV2Recipient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlarmDispatchV2RecipientRepository extends JpaRepository<AlarmDispatchV2Recipient, Long> {

    @Query("select recipient.recipientUserId from AlarmDispatchV2Recipient recipient where recipient.dispatch.id = :dispatchId order by recipient.id")
    Page<Long> findRecipientUserIds(@Param("dispatchId") Long dispatchId, Pageable pageable);
}
