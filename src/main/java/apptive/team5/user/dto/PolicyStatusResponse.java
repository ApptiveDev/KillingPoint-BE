package apptive.team5.user.dto;

import apptive.team5.user.domain.PolicyRevision;
import apptive.team5.user.domain.PolicyType;
import apptive.team5.user.domain.UserPolicyAgreementEntity;

public record PolicyStatusResponse(
        PolicyType policyType,
        boolean required,
        boolean agreed,
        Long currentRevision,
        Long latestRevision
) {
    public static PolicyStatusResponse of(PolicyType type, UserPolicyAgreementEntity agreement) {
        Long latestRevision = PolicyRevision.getLatest(type);
        boolean required = type.isRequired();

        if (agreement == null) {
            return new PolicyStatusResponse(type, required, false, 0L, latestRevision);
        }

        boolean agreed = agreement.getAgreed();
        Long currentRevision = agreement.getRevision();
        return new PolicyStatusResponse(type, required, agreed, currentRevision, latestRevision);
    }

    public boolean needsUpdate() {
        return required && (!agreed || currentRevision < latestRevision);
    }
}
