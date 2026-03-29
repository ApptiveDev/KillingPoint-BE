package apptive.team5.user.repository;

import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserPolicyAgreementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPolicyAgreementRepository extends JpaRepository<UserPolicyAgreementEntity, Long> {
    List<UserPolicyAgreementEntity> findAllByUserEntity(UserEntity userEntity);
    void deleteAllByUserEntity(UserEntity userEntity);
}
