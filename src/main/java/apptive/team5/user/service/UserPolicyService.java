package apptive.team5.user.service;

import apptive.team5.global.exception.BadRequestException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.user.domain.ClientType;
import apptive.team5.user.domain.ClientVersion;
import apptive.team5.user.domain.PolicyRevision;
import apptive.team5.user.domain.PolicyType;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserPolicyAgreementEntity;
import apptive.team5.user.dto.InitSettingsResponse;
import apptive.team5.user.dto.InitSettingsResponse.AppUpdateStatus;
import apptive.team5.user.dto.PolicyAgreementRequest;
import apptive.team5.user.dto.PolicyAgreementRequest.AgreementItem;
import apptive.team5.user.dto.PolicyStatusResponse;
import apptive.team5.user.util.VersionComparator;
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
    private final UserInitSettingService userInitSettingService;

    @Transactional(readOnly = true)
    public InitSettingsResponse getInitSettings(Long userId, ClientType clientType, String clientVersion) {
        UserEntity user = userLowService.findById(userId);

        AppUpdateStatus appUpdateStatus = checkAppUpdate(clientType, clientVersion);
        List<PolicyStatusResponse> policies = buildPolicyStatuses(user);
        boolean needsPolicyAgreement = policies.stream()
                .anyMatch(PolicyStatusResponse::needsUpdate);
        boolean needsTagSetup = userInitSettingService.checkNeedsTagSetup(user);

        return new InitSettingsResponse(appUpdateStatus, needsPolicyAgreement, needsTagSetup, policies);
    }

    public void agreePolicy(Long userId, PolicyAgreementRequest request) {
        UserEntity user = userLowService.findById(userId);

        validateRequiredPolicies(request.agreements());
        upsertAgreements(user, request.agreements());
    }

    private AppUpdateStatus checkAppUpdate(ClientType clientType, String clientVersion) {
        boolean needsForceUpdate = VersionComparator.isLowerThan(clientVersion, ClientVersion.getMinVersion(clientType));
        boolean needsOptionalUpdate = VersionComparator.isLowerThan(clientVersion, ClientVersion.getLatestVersion(clientType));
        return new AppUpdateStatus(needsForceUpdate, needsOptionalUpdate);
    }

    private List<PolicyStatusResponse> buildPolicyStatuses(UserEntity user) {
        Map<PolicyType, UserPolicyAgreementEntity> agreementMap = userPolicyLowService.getAgreementMap(user);

        return Arrays.stream(PolicyType.values())
                .map(type -> PolicyStatusResponse.of(type, agreementMap.get(type)))
                .toList();
    }

    private void validateRequiredPolicies(List<AgreementItem> agreements) {
        for (AgreementItem item : agreements) {
            if (item.policyType().isRequired() && !item.agreed()) {
                throw new BadRequestException(ExceptionCode.REQUIRED_POLICY_NOT_AGREED.getDescription());
            }
        }
    }

    private void upsertAgreements(UserEntity user, List<AgreementItem> agreements) {
        Map<PolicyType, UserPolicyAgreementEntity> agreementMap = userPolicyLowService.getAgreementMap(user);

        for (AgreementItem item : agreements) {
            Long latestRevision = PolicyRevision.getLatest(item.policyType());
            UserPolicyAgreementEntity existing = agreementMap.get(item.policyType());

            if (existing != null) {
                existing.updateAgreement(item.agreed(), latestRevision);
            } else {
                userPolicyLowService.save(
                        new UserPolicyAgreementEntity(user, item.policyType(), item.agreed(), latestRevision)
                );
            }
        }
    }
}
