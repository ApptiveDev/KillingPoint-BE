package apptive.team5.user.service;

import apptive.team5.user.domain.PolicyType;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserPolicyAgreementEntity;
import apptive.team5.user.repository.UserPolicyAgreementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPolicyLowService {

    private final UserPolicyAgreementRepository policyAgreementRepository;

    @Transactional(readOnly = true)
    public Map<PolicyType, UserPolicyAgreementEntity> getAgreementMap(UserEntity user) {
        return policyAgreementRepository.findAllByUserEntity(user)
                .stream()
                .collect(Collectors.toMap(UserPolicyAgreementEntity::getPolicyType, a -> a));
    }

    public UserPolicyAgreementEntity save(UserPolicyAgreementEntity agreement) {
        return policyAgreementRepository.save(agreement);
    }

    public void deleteByUserEntity(UserEntity user) {
        policyAgreementRepository.deleteAllByUserEntity(user);
    }
}
