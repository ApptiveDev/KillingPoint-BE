package apptive.team5.user.domain;

public enum SocialType {

    KAKAO("카카오"), GOOGLE("구글"), APPLE("애플");

    private final String description;

    SocialType(String description) {
        this.description = description;
    }
}
