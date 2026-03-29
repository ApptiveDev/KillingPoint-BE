package apptive.team5.user.service;

import apptive.team5.global.exception.BadRequestException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.user.domain.PolicyRevision;
import apptive.team5.user.domain.PolicyType;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserPolicyAgreementEntity;
import apptive.team5.user.dto.PolicyAgreementRequest;
import apptive.team5.user.dto.AgreementItem;
import apptive.team5.user.dto.PolicyStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Transactional
@Service
@RequiredArgsConstructor
public class UserPolicyService {

    private final UserLowService userLowService;
    private final UserPolicyLowService userPolicyLowService;

    public void agreePolicy(Long userId, PolicyAgreementRequest request) {
        UserEntity user = userLowService.findById(userId);

        validateRequiredPolicies(request.agreements());
        upsertAgreements(user, request.agreements());
    }

    @Transactional(readOnly = true)
    public List<PolicyStatusResponse> getPolicyStatuses(Long userId) {
        UserEntity user = userLowService.findById(userId);
        return getPolicyStatuses(user);
    }

    private List<PolicyStatusResponse> getPolicyStatuses(UserEntity user) {
        Map<PolicyType, UserPolicyAgreementEntity> agreementMap = userPolicyLowService.getAgreementMap(user);

        return Arrays.stream(PolicyType.values())
                .map(type -> PolicyStatusResponse.of(type, agreementMap.get(type)))
                .toList();
    }

    private void validateRequiredPolicies(List<AgreementItem> agreements) {
        for (AgreementItem item : agreements) {
            validateRequiredPolicy(item);
        }
    }

    private void validateRequiredPolicy(AgreementItem item) {
        if (item.policyType().isRequired() && !item.agreed()) {
            throw new BadRequestException(ExceptionCode.REQUIRED_POLICY_NOT_AGREED.getDescription());
        }
    }

    private void upsertAgreements(UserEntity user, List<AgreementItem> agreements) {
        Map<PolicyType, UserPolicyAgreementEntity> agreementMap = userPolicyLowService.getAgreementMap(user);
        for (AgreementItem item : agreements) {
            upsertAgreement(user, item, agreementMap);
        }
    }

    private void upsertAgreement(UserEntity user, AgreementItem item, Map<PolicyType, UserPolicyAgreementEntity> agreementMap) {
        UserPolicyAgreementEntity existing = agreementMap.get(item.policyType());
        Long latestRevision = PolicyRevision.getLatest(item.policyType());

        if (existing != null) {
            existing.updateAgreement(item.agreed(), latestRevision);
        }
        else {
            userPolicyLowService.save(
                    new UserPolicyAgreementEntity(
                            user,
                            item.policyType(),
                            item.agreed(),
                            latestRevision
                    )
            );
        }
    }
}
