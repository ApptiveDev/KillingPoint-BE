package apptive.team5.oauth2.repository;

import apptive.team5.oauth2.domain.AppleRefreshToken;
import apptive.team5.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AppleRefreshTokenRepository extends JpaRepository<AppleRefreshToken, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from AppleRefreshToken ar where ar.user = :user")
    void deleteByUser(UserEntity user);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from AppleRefreshToken ar where ar.user.id = :userId")
    void deleteByUserId(Long userId);


    @Query("select ar from AppleRefreshToken ar where ar.user.id = :userId")
    Optional<AppleRefreshToken> findByUserId(Long userId);

}
