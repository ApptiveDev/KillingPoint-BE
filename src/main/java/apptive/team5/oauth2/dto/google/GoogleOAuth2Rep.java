package apptive.team5.oauth2.dto.google;

import apptive.team5.oauth2.dto.OAuth2Response;
import apptive.team5.user.domain.SocialType;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

public record GoogleOAuth2Rep
        (GoogleIdToken.Payload payload) implements OAuth2Response

{
    @Override
    public SocialType getProvider() {
        return SocialType.GOOGLE;
    }

    @Override
    public String getProviderId() {
        return payload.getSubject();
    }

    @Override
    public String getUsername() {
        return (String) payload.get("name");
    }

    @Override
    public String getEmail() {
        return payload.getEmail();
    }
}
