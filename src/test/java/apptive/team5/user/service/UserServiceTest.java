package apptive.team5.user.service;

import apptive.team5.file.dto.FileUploadRequest;
import apptive.team5.file.service.S3Service;
import apptive.team5.file.service.TemporalLowService;
import apptive.team5.global.exception.DuplicateException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.global.exception.NotFoundEntityException;
import apptive.team5.global.util.S3Util;
import apptive.team5.jwt.component.JWTUtil;
import apptive.team5.jwt.dto.TokenResponse;
import apptive.team5.jwt.service.JwtService;
import apptive.team5.oauth2.dto.google.GoogleOAuth2Rep;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.dto.UserResponse;
import apptive.team5.user.dto.UserTagUpdateRequest;
import apptive.team5.util.TestUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.BDDMockito.*;


@ExtendWith(SpringExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserLowService userLowService;

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private JwtService jwtService;

    @Mock
    private S3Service s3Service;

    @Mock
    private TemporalLowService  temporalLowService;

    @Test
    @DisplayName("소셜 로그인 - 존재하는 회원이면 로그인")
    void socialLoginCase1() {
        // given
        UserEntity user = TestUtil.makeUserEntity();

        given(userLowService.existsByIdentifier(any()))
                .willReturn(true);

        given(userLowService.findByIdentifier(any()))
                .willReturn(user);

        given(jwtUtil.createJWT(any(), any(), any()))
                .willReturn("accessToken", "refreshToken");

        TokenResponse tokenResponse = userService.socialLogin(socialLoginCase());

        assertSoftly(softly -> {
            softly.assertThat(tokenResponse.accessToken()).isEqualTo("accessToken");
            softly.assertThat(tokenResponse.refreshToken()).isEqualTo("refreshToken");
        });
        verify(userLowService).existsByIdentifier(any());
        verify(userLowService).findByIdentifier(any());
        verify(jwtService).saveRefreshToken(any(),any());
        verify(jwtUtil, times(2)).createJWT(any(), any(), any());
        verifyNoMoreInteractions(userLowService, jwtUtil,jwtService);
    }

    @Test
    @DisplayName("소셜 로그인 - 존재하지 않는 회원이면 회원가입")
    void socialLoginCase2() {
        // given
        UserEntity user = TestUtil.makeUserEntity();

        given(userLowService.existsByIdentifier(any()))
                .willReturn(false);

        given(userLowService.save(any()))
                .willReturn(user);

        given(jwtUtil.createJWT(any(), any(), any()))
                .willReturn("accessToken", "refreshToken");

        TokenResponse tokenResponse = userService.socialLogin(socialLoginCase());

        assertSoftly(softly -> {
            softly.assertThat(tokenResponse.accessToken()).isEqualTo("accessToken");
            softly.assertThat(tokenResponse.refreshToken()).isEqualTo("refreshToken");
        });
        verify(userLowService).existsByIdentifier(any());
        verify(userLowService).save(any());
        verify(jwtService).saveRefreshToken(any(),any());
        verify(jwtUtil, times(2)).createJWT(any(), any(), any());
        verifyNoMoreInteractions(userLowService, jwtUtil, jwtService);
    }

    @Test
    @DisplayName("tag 변경 성공")
    void changeTagSuccess() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();
        UserTagUpdateRequest userTagUpdateRequest = new UserTagUpdateRequest("aaa");

        given(userLowService.findById(any()))
               .willReturn(user);

       given((userLowService.existsByTag(any())))
               .willReturn(false);

       // when
        UserResponse userResponse = userService.changeTag(userTagUpdateRequest, user.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(userResponse.tag()).isEqualTo(userTagUpdateRequest.tag());
            softly.assertThat(userResponse.userId()).isEqualTo(user.getId());
        });

        verify(userLowService).existsByTag(any());
        verify(userLowService).findById(any());
        verifyNoMoreInteractions(userLowService, jwtUtil, jwtService);
    }

    @Test
    @DisplayName("tag 변경 실패 - 중복된 태그")
    void changeTagFail() {
        // given
        UserEntity user = TestUtil.makeUserEntityWithId();
        UserTagUpdateRequest userTagUpdateRequest = new UserTagUpdateRequest("aaa");

        given(userLowService.findById(any()))
                .willReturn(user);

        given((userLowService.existsByTag(any())))
                .willReturn(true);

        // when
        Assertions.assertThatThrownBy(()->userService.changeTag(userTagUpdateRequest, user.getId()))
                .isInstanceOf(DuplicateException.class)
                .hasMessage(ExceptionCode.DUPLICATE_USER_TAG.getDescription());


        verify(userLowService).existsByTag(any());
        verify(userLowService).findById(any());
        verifyNoMoreInteractions(userLowService, jwtUtil, jwtService);
    }

    @Test
    @DisplayName("프로필 이미지 변환 성공")
    void profileImageChangeSuccess() {

        // given
        UserEntity userEntity = TestUtil.makeUserEntityWithId();

        FileUploadRequest fileUploadRequest = new FileUploadRequest(1L, "updateImageUrl");

        given(userLowService.findById(any()))
                .willReturn(userEntity);

        // when
        UserResponse userResponse = userService.changeProfileImage(fileUploadRequest, userEntity.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(userResponse.userId()).isEqualTo(userEntity.getId());
            softly.assertThat(S3Util.extractFileName(userResponse.profileImageUrl())).isEqualTo(fileUploadRequest.presignedUrl());
        });


        verify(userLowService).findById(any());
        verify(s3Service).deleteS3File(any());
        verify(temporalLowService).deleteById(any());
        verifyNoMoreInteractions(userLowService,s3Service, temporalLowService);

    }

    @Test
    @DisplayName("프로필 이미지 변환 실패 - 존재하지 않는 회원")
    void profileImageChangeFail() {

        // given

        given(userLowService.findById(any()))
                .willThrow(NotFoundEntityException.class);

        // when & then
        assertThatThrownBy(()->
                userService.changeProfileImage(new FileUploadRequest(1L,"updateImageUrl"), 1L))
        .isInstanceOf(NotFoundEntityException.class);
        verify(userLowService).findById(any());
        verifyNoMoreInteractions(userLowService,s3Service, temporalLowService);

    }

    private GoogleOAuth2Rep socialLoginCase() {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();

        payload.setSubject(TestUtil.userIdentifier);
        payload.set("name", "이진원");
        payload.set("email", "example@naver.com");

        return new GoogleOAuth2Rep(payload);
    }

}
