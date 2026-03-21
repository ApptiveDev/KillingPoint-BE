package apptive.team5.oauth2.component;

import apptive.team5.global.exception.AuthenticationException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.jwt.component.JWTUtil;
import apptive.team5.oauth2.dto.apple.ApplePublicKeyResponse;
import apptive.team5.oauth2.dto.apple.AppleTokenResponse;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.security.PublicKey;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class AppleApiConnector {

    @Value("${apple.auth.public-key-url}")
    private String applePublicKeysUrl;
    @Value("${apple.auth.token-url}")
    private String appleTokenUrl;
    @Value("${apple.auth.revoke-url}")
    private String appleRevokeUrl;
    @Value("${apple.clientId}")
    private String clientId;
    @Value("${apple.issuer}")
    private String issuer;

    private final RestClient restClient;
    private final AppleKeyGenerator appleKeyGenerator;
    private final JWTUtil jwtUtil;

    public AppleApiConnector(RestClient.Builder builder, AppleKeyGenerator appleKeyGenerator, JWTUtil jwtUtil) {
        this.restClient = builder.build();
        this.appleKeyGenerator = appleKeyGenerator;
        this.jwtUtil = jwtUtil;
    }

    public AppleTokenResponse getAppleRefreshToken(String authorizationCode) {
        MultiValueMap<String, String> body = getCreateTokenBody(authorizationCode);

        AppleTokenResponse appleTokenResponse = restClient.post()
                .uri(appleTokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(AppleTokenResponse.class);

        return appleTokenResponse;
    }


    public void revokeToken(String refreshToken) {
        MultiValueMap<String, String> body = getRevokeTokenBody(refreshToken);

        restClient.post()
                .uri(appleRevokeUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    public Claims verifyIdentityToken(String identityToken) {
        Map<String, String> headers = jwtUtil.parseHeaders(identityToken);
        ApplePublicKeyResponse appleAuthPublicKey = getAppleAuthPublicKey();
        PublicKey publicKey = appleKeyGenerator.generatePublicKey(headers, appleAuthPublicKey);

        Claims tokenClaims = jwtUtil.getAppleIdentityTokenClaims(identityToken, publicKey);

        if (!issuer.equals(tokenClaims.getIssuer())) {
            log.info("Issuer mismatch");
            throw new AuthenticationException(ExceptionCode.INVALID_TOKEN.getDescription());
        }

        Set<String> audience = tokenClaims.getAudience();
        log.info("Apple identity token audience: {}", audience);

        if (audience == null || !audience.contains(clientId)) {
            log.info("audience mismatch");
            throw new AuthenticationException(ExceptionCode.INVALID_TOKEN.getDescription());
        }

        return tokenClaims;
    }

    private ApplePublicKeyResponse getAppleAuthPublicKey() {
        return restClient.get()
                .uri(applePublicKeysUrl)
                .retrieve()
                .body(ApplePublicKeyResponse.class);
    }

    private MultiValueMap<String, String> getRevokeTokenBody(String refreshToken) {

        String clientSecret = appleKeyGenerator.getClientSecret();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("token", refreshToken);
        body.add("client_secret", clientSecret);
        body.add("token_type_hint", "refresh_token");
        return body;
    }

    private MultiValueMap<String, String> getCreateTokenBody(String authorizationCode) {

        String clientSecret = appleKeyGenerator.getClientSecret();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authorizationCode);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("grant_type", "authorization_code");
        return body;
    }


}
