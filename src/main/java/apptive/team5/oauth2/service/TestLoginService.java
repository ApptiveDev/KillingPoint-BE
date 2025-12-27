package apptive.team5.oauth2.service;

import apptive.team5.global.exception.NotFoundEntityException;
import apptive.team5.jwt.TokenType;
import apptive.team5.jwt.component.JWTUtil;
import apptive.team5.jwt.dto.TokenResponse;
import apptive.team5.jwt.service.JwtService;
import apptive.team5.user.domain.SocialType;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserRoleType;
import apptive.team5.user.repository.UserRepository;
import apptive.team5.user.service.UserLowService;
import apptive.team5.user.service.UserService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class TestLoginService {

    private final UserRepository userRepository;
    private static final String TEST_IDENTIFIER = "TEST-IDENTIFIER";
    private final JWTUtil jwtUtil;
    private final JwtService jwtService;

    public TokenResponse testLogin() {

        UserEntity user;

        Optional<UserEntity> findUser = userRepository.findByIdentifier(TEST_IDENTIFIER);


        if(findUser.isPresent()){
            user = findUser.get();
        }
        else {
            user = userRepository.save(new UserEntity(TEST_IDENTIFIER, "test@naver.com", "tester", "tester", UserRoleType.USER, SocialType.KAKAO));
        }



        String accessToken = jwtUtil.createJWT(user.getId(), "ROLE_" + user.getRoleType().name(), TokenType.ACCESS_TOKEN);
        String refreshToken = jwtUtil.createJWT(user.getId(), "ROLE_" + user.getRoleType().name(), TokenType.REFRESH_TOKEN);

        jwtService.saveRefreshToken(user.getId(), refreshToken);

        return new TokenResponse(accessToken, refreshToken, true);
    }
}
