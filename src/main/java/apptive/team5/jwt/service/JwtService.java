package apptive.team5.jwt.service;

import apptive.team5.global.exception.AuthenticationException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.jwt.TokenType;
import apptive.team5.jwt.component.JWTUtil;
import apptive.team5.jwt.domain.RefreshToken;
import apptive.team5.jwt.dto.TokenResponse;
import apptive.team5.jwt.component.RefreshTokenEncoder;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class JwtService {

    private final JWTUtil jwtUtil;
    private final RefreshTokenEncoder tokenEncoder;
    private final UserLowService userLowService;
    private final JwtLowService jwtLowService;

    public RefreshToken saveRefreshToken(Long userId, String refreshToken) {

        UserEntity findUser = userLowService.findById(userId);

        String encodedRefreshToken = tokenEncoder.encode(refreshToken);

        jwtLowService.deleteByUser(findUser);

        return jwtLowService.save(new RefreshToken(findUser, encodedRefreshToken));
    }

    public TokenResponse exchangeToken(String oldRefreshToken) {

        if (oldRefreshToken == null) throw new AuthenticationException(ExceptionCode.NOT_EXIST_REFRESH_TOKEN.getDescription());

        if (!jwtUtil.validateToken(oldRefreshToken, TokenType.REFRESH_TOKEN))
            throw new AuthenticationException(ExceptionCode.INVALID_REFRESH_TOKEN.getDescription());

        Claims claims = jwtUtil.getClaims(oldRefreshToken);
        Long userId = Long.valueOf(claims.get("userId").toString());
        String role = claims.get("role").toString();

        RefreshToken findRefreshToken = jwtLowService.findByUserId(userId);

        if (!tokenEncoder.match(findRefreshToken.getToken(), oldRefreshToken)) {
            throw new AuthenticationException(ExceptionCode.INVALID_REFRESH_TOKEN.getDescription());
        }

        String newAccessToken = jwtUtil.createJWT(userId, role, TokenType.ACCESS_TOKEN);
        String newRefreshToken = jwtUtil.createJWT(userId, role, TokenType.REFRESH_TOKEN);

        saveRefreshToken(userId, newRefreshToken);

        return new TokenResponse(newAccessToken, newRefreshToken, false);
    }

    public void deleteRefreshTokenByUserId(Long userId) {
        jwtLowService.deleteByUserId(userId);
    }

    public void deleteExpiredRefreshTokens() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(8);
        jwtLowService.deleteExpiredRefreshToken(cutoff);
    }
}
