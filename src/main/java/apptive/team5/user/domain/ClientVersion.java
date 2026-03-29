package apptive.team5.user.domain;

import java.util.Map;

public class ClientVersion {

    private static final Map<ClientType, String> MIN_VERSION = Map.of(
            ClientType.ANDROID, "1.0.0",
            ClientType.IOS, "1.0.0"
    );

    private static final Map<ClientType, String> LATEST_VERSION = Map.of(
            ClientType.ANDROID, "1.0.0",
            ClientType.IOS, "1.0.0"
    );

    public static String getMinVersion(ClientType type) {
        return MIN_VERSION.get(type);
    }

    public static String getLatestVersion(ClientType type) {
        return LATEST_VERSION.get(type);
    }
}
