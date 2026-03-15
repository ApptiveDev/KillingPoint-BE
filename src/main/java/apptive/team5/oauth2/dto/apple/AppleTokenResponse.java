package apptive.team5.oauth2.dto.apple;

public record AppleTokenResponse (
        String access_token,
        String expires_in,
        String id_token,
        String refresh_token,
        String token_type
){
}
