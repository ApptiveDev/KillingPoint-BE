package apptive.team5.jwt.component;

import apptive.team5.global.exception.AuthenticationException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.jwt.TokenType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JWTUtil {

    private SecretKey secretKey;
    private final ObjectMapper objectMapper;
    private static final Long accessTokenExpiresIn = 3600L * 1000; // 1시간
    private static final Long refreshTokenExpiresIn = 604800L * 1000; // 7일;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }


    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, TokenType tokenType) {
        try {
            Claims claims = getClaims(token);
            String realTokenType = claims.get("tokenType").toString();

            if (tokenType == null) return false;
            if (!realTokenType.equals(tokenType.name())) return false;

            return true;
        } catch (Exception ex) {
            return false;
        }
    }


    public String createJWT(Long userId, String role, TokenType tokenType) {

        long expiredMs;
        String type;

        if (tokenType.equals(TokenType.ACCESS_TOKEN)) {
            expiredMs = accessTokenExpiresIn;
            type = TokenType.ACCESS_TOKEN.name();
        }
        else {
            expiredMs = refreshTokenExpiresIn;
            type = TokenType.REFRESH_TOKEN.name();
        }

        return Jwts.builder()
                .claim("userId", userId)
                .claim("role",role)
                .claim("tokenType",type)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public String createJWT(String userId, String role, TokenType tokenType, Long expiredMs) {

        String type;

        if (tokenType.equals(TokenType.ACCESS_TOKEN)) {
            type = TokenType.ACCESS_TOKEN.name();
        }
        else {
            type = TokenType.REFRESH_TOKEN.name();
        }

        return Jwts.builder()
                .claim("userId", userId)
                .claim("role",role)
                .claim("tokenType",type)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public Map<String, String> parseHeaders(String token) {
        try {
            String header = token.split("\\.")[0];
            return objectMapper.readValue(decodeHeader(header), Map.class);
        } catch (Exception ex) {
            throw new AuthenticationException(ExceptionCode.INVALID_TOKEN.getDescription());
        }
    }

    private String decodeHeader(String token) {
        return new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
    }

    public Claims getAppleIdentityTokenClaims(String token, PublicKey publicKey) {
        try {
            return Jwts.parser()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (Exception ex) {
            throw new AuthenticationException(ExceptionCode.INVALID_TOKEN.getDescription());
        }
    }
}
