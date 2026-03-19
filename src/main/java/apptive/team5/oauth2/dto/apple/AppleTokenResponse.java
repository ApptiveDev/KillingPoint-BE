package apptive.team5.oauth2.dto.apple;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AppleTokenResponse (
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("expires_in")
        String expiresIn,
        @JsonProperty("id_token")
        String idToken,
        @JsonProperty("refresh_token")
        String refreshToken,
        @JsonProperty("token_type")
        String tokenType
){
}
