package apptive.team5.jwt.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        boolean isNew
) {
}
