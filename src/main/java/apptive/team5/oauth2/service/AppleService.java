package apptive.team5.oauth2.service;

import apptive.team5.jwt.dto.TokenResponse;
import apptive.team5.oauth2.component.AppleApiConnector;
import apptive.team5.oauth2.dto.apple.AppleLoginRequest;
import apptive.team5.oauth2.dto.apple.AppleOAuth2Rep;
import apptive.team5.user.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class AppleService {

    private final UserService userService;
    private final AppleApiConnector appleApiConnector;


    public TokenResponse appleLogin(AppleLoginRequest appleLoginRequest) {
        Claims claims = appleApiConnector.verifyIdentityToken(appleLoginRequest.identityToken());

        String appleId = claims.getSubject();

        return userService.socialLogin(new AppleOAuth2Rep(appleId, appleLoginRequest.email(), appleLoginRequest.authorizationCode()));
    }


}
