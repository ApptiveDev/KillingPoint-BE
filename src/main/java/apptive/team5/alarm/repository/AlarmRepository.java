package apptive.team5.alarm.repository;

import apptive.team5.alarm.entity.Alarm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    @Query("select al from Alarm al where al.user.id = :userId")
    Page<Alarm> findByUserIdWithPage(Long userId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Alarm al where al.user.id = :userId")
    void deleteByUserId(Long userId);
}
