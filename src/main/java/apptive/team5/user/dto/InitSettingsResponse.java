package apptive.team5.user.dto;

import java.util.List;

public record InitSettingsResponse(
        boolean needsUpdate,
        boolean needsTagSetup,
        List<PolicyStatusResponse> policies
) {
}
