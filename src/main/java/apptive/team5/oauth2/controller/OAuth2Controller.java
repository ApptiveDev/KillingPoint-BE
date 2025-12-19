package apptive.team5.oauth2.controller;

import apptive.team5.jwt.dto.TokenResponse;
import apptive.team5.oauth2.dto.GoogleLoginRequest;
import apptive.team5.oauth2.dto.KakaoLoginRequest;
import apptive.team5.oauth2.service.GoogleService;
import apptive.team5.oauth2.service.KakaoService;
import apptive.team5.oauth2.service.TestLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth2")
public class OAuth2Controller {

    private final KakaoService kakaoService;
    private final GoogleService googleService;
    private final TestLoginService testLoginService;

    @PostMapping("/kakao")
    public ResponseEntity<TokenResponse> kakaoLogin(@RequestBody KakaoLoginRequest kakaoLoginRequest) {

        TokenResponse tokenResponse = kakaoService.kakaoLogin(kakaoLoginRequest.accessToken());

        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/google")
    public ResponseEntity<TokenResponse> googleLogin(@RequestBody GoogleLoginRequest googleLoginRequest) {

        TokenResponse tokenResponse = googleService.googleLogin(googleLoginRequest.idToken());

        return  ResponseEntity.ok(tokenResponse);
    }

    @GetMapping("/test")
    public ResponseEntity<TokenResponse> testLogin() {

        TokenResponse tokenResponse = testLoginService.testLogin();

        return ResponseEntity.ok(tokenResponse);
    }
}
