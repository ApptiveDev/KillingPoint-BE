package apptive.team5.user.dto;

import apptive.team5.user.domain.PolicyType;

public record PolicyStatusResponse(
        PolicyType policyType,
        boolean required,
        boolean agreed,
        Long currentRevision,
        Long latestRevision
) {
}
