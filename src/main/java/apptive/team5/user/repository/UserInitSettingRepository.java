package apptive.team5.user.repository;

import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserInitSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInitSettingRepository extends JpaRepository<UserInitSettingEntity, Long> {
    Optional<UserInitSettingEntity> findByUserEntity(UserEntity userEntity);
    void deleteByUserEntity(UserEntity userEntity);
}
