package apptive.team5.oauth2.service;

import apptive.team5.global.exception.AuthenticationException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.jwt.domain.RefreshToken;
import apptive.team5.jwt.repository.RefreshTokenRepository;
import apptive.team5.oauth2.domain.AppleRefreshToken;
import apptive.team5.oauth2.dto.apple.AppleTokenResponse;
import apptive.team5.oauth2.repository.AppleRefreshTokenRepository;
import apptive.team5.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Transactional
@Service
@RequiredArgsConstructor
public class AppleRefreshTokenLowService {

    private final AppleRefreshTokenRepository appleRefreshTokenRepository;

    @Transactional(readOnly = true)
    public AppleRefreshToken findByUserId(Long userId) {
        return appleRefreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthenticationException(ExceptionCode.NOT_EXIST_REFRESH_TOKEN.getDescription()));
    }

    public void deleteByUser(UserEntity user) {
        appleRefreshTokenRepository.deleteByUser(user);
    }

    public void deleteByUserId(Long userId) {
        appleRefreshTokenRepository.deleteByUserId(userId);
    }

    public AppleRefreshToken save(AppleRefreshToken appleRefreshToken) {
        return appleRefreshTokenRepository.save(appleRefreshToken);
    }

}
