package apptive.team5.oauth2.service;

import apptive.team5.global.exception.AuthenticationException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.jwt.component.JWTUtil;
import apptive.team5.jwt.dto.TokenResponse;
import apptive.team5.oauth2.component.AppleKeyGenerator;
import apptive.team5.oauth2.dto.apple.AppleLoginRequest;
import apptive.team5.oauth2.dto.apple.AppleOAuth2Rep;
import apptive.team5.oauth2.dto.apple.ApplePublicKeyResponse;
import apptive.team5.user.service.UserService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.security.PublicKey;
import java.util.Map;


@Service
public class AppleService {

    @Value("${apple.auth.public-key-url}")
    private String publicKeyUrl;
    @Value("${apple.issuer}")
    private String issuer;
    @Value("${apple.clientId}")
    private String clientId;

    private final RestClient restClient;
    private final JWTUtil jwtUtil;
    private final AppleKeyGenerator applePublicKeyGenerator;
    private final UserService userService;

    public AppleService(RestClient.Builder builder, JWTUtil jwtUtil, AppleKeyGenerator applePublicKeyGenerator, UserService userService) {
        this.restClient = builder.build();
        this.jwtUtil = jwtUtil;
        this.applePublicKeyGenerator = applePublicKeyGenerator;
        this.userService = userService;
    }

    public TokenResponse appleLogin(AppleLoginRequest appleLoginRequest) {
        Claims claims = verifyIdentityToken(appleLoginRequest.identityToken());

        String appleId = claims.getSubject();
        String email = claims.get("email", String.class);

        return userService.socialLogin(new AppleOAuth2Rep(appleId, email));
    }

    private Claims verifyIdentityToken(String identityToken) {
        Map<String, String> headers = jwtUtil.parseHeaders(identityToken);
        ApplePublicKeyResponse appleAuthPublicKey = getAppleAuthPublicKey();
        PublicKey publicKey = applePublicKeyGenerator.generatePublicKey(headers, appleAuthPublicKey);

        Claims tokenClaims = jwtUtil.getTokenClaims(identityToken, publicKey);

        if (!issuer.equals(tokenClaims.getIssuer())) {
            throw new AuthenticationException(ExceptionCode.INVALID_TOKEN.getDescription());
        }

        if (!clientId.equals(tokenClaims.getAudience())) {
            throw new AuthenticationException(ExceptionCode.INVALID_TOKEN.getDescription());
        }

        return tokenClaims;
    }

    private ApplePublicKeyResponse getAppleAuthPublicKey() {
        return restClient.get()
                .uri(publicKeyUrl)
                .retrieve()
                .body(ApplePublicKeyResponse.class);
    }
}
