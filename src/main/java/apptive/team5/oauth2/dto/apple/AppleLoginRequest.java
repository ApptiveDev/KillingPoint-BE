package apptive.team5.oauth2.dto.apple;

import jakarta.validation.constraints.NotBlank;

public record AppleLoginRequest(
        @NotBlank
        String identityToken,
        @NotBlank
        String authorizationCode,
        @NotBlank
        String email,
        @NotBlank
        String name
) {
}
