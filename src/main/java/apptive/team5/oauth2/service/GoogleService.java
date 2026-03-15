package apptive.team5.oauth2.service;

import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.global.exception.ExternalApiConnectException;
import apptive.team5.jwt.dto.TokenResponse;
import apptive.team5.oauth2.dto.google.GoogleOAuth2Rep;
import apptive.team5.user.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GoogleService {

    @Value("${google.client.id}")
    private String googleClientId;

    private final UserService userService;

    public TokenResponse googleLogin(String idToken) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            GoogleOAuth2Rep googleOAuth2Rep = new GoogleOAuth2Rep(googleIdToken.getPayload());

            return userService.socialLogin(googleOAuth2Rep);
        } catch (IOException | GeneralSecurityException e) {
            throw new ExternalApiConnectException(ExceptionCode.GOOGLE_API_EXCEPTION.getDescription(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
