package apptive.team5.oauth2.service;

import apptive.team5.jwt.TokenType;
import apptive.team5.jwt.component.JWTUtil;
import apptive.team5.jwt.dto.TokenResponse;
import apptive.team5.jwt.service.JwtService;
import apptive.team5.user.domain.SocialType;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserRoleType;
import apptive.team5.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class TestLoginService {

    private static final String DEFAULT_TEST_NAME = "tester";
    private static final String TEST_IDENTIFIER_PREFIX = "TEST-";
    private static final String DEFAULT_TEST_IDENTIFIER = "TEST-IDENTIFIER";

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final JwtService jwtService;

    public TokenResponse testLogin(String name) {
        String testName = (name == null || name.isBlank()) ? DEFAULT_TEST_NAME : name.trim();
        String identifier = DEFAULT_TEST_NAME.equals(testName) ? DEFAULT_TEST_IDENTIFIER : TEST_IDENTIFIER_PREFIX + testName;

        UserEntity user = userRepository.findByIdentifier(identifier)
                .orElseGet(() -> userRepository.save(new UserEntity(
                        identifier,
                        testName + "@test.local",
                        testName,
                        testName,
                        UserRoleType.USER,
                        SocialType.KAKAO
                )));

        String accessToken = jwtUtil.createJWT(user.getId(), "ROLE_" + user.getRoleType().name(), TokenType.ACCESS_TOKEN);
        String refreshToken = jwtUtil.createJWT(user.getId(), "ROLE_" + user.getRoleType().name(), TokenType.REFRESH_TOKEN);

        jwtService.saveRefreshToken(user.getId(), refreshToken);

        return new TokenResponse(accessToken, refreshToken, true);
    }
}
