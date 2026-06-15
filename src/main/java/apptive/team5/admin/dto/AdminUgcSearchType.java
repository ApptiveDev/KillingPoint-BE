package apptive.team5.admin.dto;

public enum AdminUgcSearchType {
    MUSIC_TITLE("곡 제목"),
    ARTIST("아티스트"),
    USERNAME("작성자");

    private final String label;

    AdminUgcSearchType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static AdminUgcSearchType from(String value) {
        for (AdminUgcSearchType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return MUSIC_TITLE;
    }
}
