package apptive.team5.admin.dto;

public enum UserSearchType {
    USER_ID("User ID"),
    EMAIL("Email"),
    TAG("Tag"),
    USERNAME("Username");

    private final String label;

    UserSearchType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static UserSearchType from(String value) {
        for (UserSearchType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return USER_ID;
    }
}
