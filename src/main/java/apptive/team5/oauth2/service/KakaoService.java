package apptive.team5.oauth2.service;

import apptive.team5.jwt.component.JWTUtil;
import apptive.team5.jwt.dto.TokenResponse;
import apptive.team5.oauth2.dto.kakao.KakaoOAuth2Rep;
import apptive.team5.oauth2.dto.OAuth2Response;
import apptive.team5.oauth2.handler.KakaoApiResponseHandler;
import apptive.team5.oauth2.properties.KakaoProperties;
import apptive.team5.user.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class KakaoService {

    private final RestClient restClient;
    private final UserService userService;
    private final KakaoProperties kakaoProperties;

    public KakaoService(RestClient.Builder builder, UserService userService, KakaoProperties kakaoProperties, JWTUtil jwtUtil) {
        this.restClient = builder.build();
        this.userService = userService;
        this.kakaoProperties = kakaoProperties;
    }

    public TokenResponse kakaoLogin(String kakaoAccessToken) {

        OAuth2Response userInfo = getUserInfo(kakaoAccessToken);

        return userService.socialLogin(userInfo);
    }

    public OAuth2Response getUserInfo(String accessToken) {

        ResponseEntity<Map> userInfoResponse = restClient.get()
                .uri(kakaoProperties.getkApiUri() + "/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED.toString())
                .retrieve()
                .onStatus(new KakaoApiResponseHandler())
                .toEntity(Map.class);


        return new KakaoOAuth2Rep(userInfoResponse.getBody());
    }
}
