package apptive.team5.oauth2.dto.apple;

import apptive.team5.oauth2.dto.OAuth2Response;
import apptive.team5.user.domain.SocialType;

public record AppleOAuth2Rep (
        String appleId,
        String email
) implements OAuth2Response {
    @Override
    public SocialType getProvider() {
        return SocialType.APPLE;
    }

    @Override
    public String getProviderId() {
        return appleId;
    }

    @Override
    public String getUsername() {
        return appleId;
    }

    @Override
    public String getEmail() {
        return email;
    }
}
