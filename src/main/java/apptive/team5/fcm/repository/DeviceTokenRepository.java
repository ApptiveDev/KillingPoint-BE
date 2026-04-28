package apptive.team5.fcm.repository;

import apptive.team5.fcm.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from DeviceToken dt where dt.user.id = :userId")
    void deleteByUserId(Long userId);

    List<DeviceToken> findByUserId(Long userId);

    Optional<DeviceToken> findByToken(String token);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from DeviceToken dt where dt.token = :token")
    void deleteByToken(String token);
}
