package apptive.team5.oauth2.dto.kakao;

import jakarta.validation.constraints.NotBlank;

public record KakaoLoginRequest(
        @NotBlank
        String accessToken
) {
}
