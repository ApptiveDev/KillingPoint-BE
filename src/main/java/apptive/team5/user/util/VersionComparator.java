package apptive.team5.user.util;

public class VersionComparator {

    public static int compare(String v1, String v2) {
        int[] parts1 = parseVersion(v1);
        int[] parts2 = parseVersion(v2);

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int num1 = getPartOrZero(parts1, i);
            int num2 = getPartOrZero(parts2, i);

            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return 0;
    }

    private static int[] parseVersion(String version) {
        String[] parts = version.split("\\.");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i]);
        }
        return result;
    }

    private static int getPartOrZero(int[] parts, int index) {
        if (index < parts.length) {
            return parts[index];
        }
        return 0;
    }

    public static boolean isLowerThan(String clientVersion, String targetVersion) {
        return compare(clientVersion, targetVersion) < 0;
    }
}
