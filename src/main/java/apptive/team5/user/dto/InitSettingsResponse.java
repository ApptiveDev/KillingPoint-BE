package apptive.team5.user.dto;

import java.util.List;

public record InitSettingsResponse(
        AppUpdateStatus app,
        boolean needsPolicyAgreement,
        boolean needsTagSetup,
        List<PolicyStatusResponse> policies
) {
    public record AppUpdateStatus(
            boolean needsForceUpdate,
            boolean needsOptionalUpdate
    ) {
    }
}
