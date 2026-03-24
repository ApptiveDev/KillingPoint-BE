package apptive.team5.user.domain;

import java.util.Map;

public class PolicyRevision {

    private static final Map<PolicyType, Long> LATEST = Map.of(
            PolicyType.SERVICE_TERMS, 1L,
            PolicyType.PRIVACY, 1L
    );

    public static Long getLatest(PolicyType type) {
        return LATEST.get(type);
    }
}
