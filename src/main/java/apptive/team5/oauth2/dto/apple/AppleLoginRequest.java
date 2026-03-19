package apptive.team5.oauth2.dto.apple;

public record AppleLoginRequest(
        String identityToken,
        String authorizationCode
) {
}
