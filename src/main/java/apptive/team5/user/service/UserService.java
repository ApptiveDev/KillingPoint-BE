package apptive.team5.user.service;
import apptive.team5.diary.service.*;
import apptive.team5.file.dto.FileUploadRequest;
import apptive.team5.file.service.S3Service;
import apptive.team5.file.service.TemporalLowService;
import apptive.team5.global.exception.DuplicateException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.global.util.S3Util;
import apptive.team5.jwt.TokenType;
import apptive.team5.jwt.component.JWTUtil;
import apptive.team5.jwt.dto.TokenResponse;
import apptive.team5.jwt.service.JwtService;
import apptive.team5.oauth2.component.AppleApiConnector;
import apptive.team5.oauth2.component.AppleKeyGenerator;
import apptive.team5.oauth2.domain.AppleRefreshToken;
import apptive.team5.oauth2.dto.OAuth2Response;
import apptive.team5.oauth2.dto.apple.AppleOAuth2Rep;
import apptive.team5.oauth2.dto.apple.AppleTokenResponse;
import apptive.team5.oauth2.service.AppleRefreshTokenLowService;
import apptive.team5.subscribe.service.SubscribeLowService;
import apptive.team5.survey.service.SurveyLowService;
import apptive.team5.user.domain.SocialType;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserRoleType;
import apptive.team5.user.dto.UserResponse;
import apptive.team5.user.dto.UserSearchResponse;
import apptive.team5.user.dto.UserStaticsResponse;
import apptive.team5.user.dto.UserTagUpdateRequest;
import apptive.team5.user.util.TagGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserLowService userLowService;
    private final JwtService jwtService;
    private final JWTUtil jwtUtil;
    private final S3Service s3Service;
    private final TemporalLowService temporalLowService;
    private final SubscribeLowService subscribeLowService;
    private final DiaryLowService diaryLowService;
    private final DiaryLikeLowService diaryLikeLowService;
    private final DiaryOrderLowService diaryOrderLowService;
    private final DiaryService diaryService;
    private final SurveyLowService surveyLowService;
    private final DiaryReportLowService diaryReportLowService;
    private final DiaryStoreLowService diaryStoreLowService;
    private final AppleApiConnector appleApiConnector;
    private final AppleRefreshTokenLowService appleRefreshTokenLowService;

    public TokenResponse socialLogin(OAuth2Response oAuth2Response) {
        String identifier = oAuth2Response.getProvider() + "-" +oAuth2Response.getProviderId();

        boolean isNew = false;

        UserEntity user;
        if (userLowService.existsByIdentifier(identifier)) {
            user = userLowService.findByIdentifier(identifier);
        }
        else {
            String tag = TagGenerator.generateTag();
            user = userLowService.save(new UserEntity(identifier, oAuth2Response.getEmail(), oAuth2Response.getUsername(), tag, UserRoleType.USER, oAuth2Response.getProvider()));
            isNew = true;
            if (oAuth2Response.getProvider().equals(SocialType.APPLE)) joinAppleUser(user, oAuth2Response);
        }

        String accessToken = jwtUtil.createJWT(user.getId(), "ROLE_" + user.getRoleType().name(), TokenType.ACCESS_TOKEN);
        String refreshToken = jwtUtil.createJWT(user.getId(), "ROLE_" + user.getRoleType().name(), TokenType.REFRESH_TOKEN);


        jwtService.saveRefreshToken(user.getId(), refreshToken);

        return new TokenResponse(accessToken, refreshToken, isNew);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserInfo(Long userId) {
        UserEntity findUser = userLowService.findById(userId);

        return new UserResponse(findUser);
    }

    public void deleteUser(Long userId) {

        UserEntity findUser = userLowService.findById(userId);
        log.info("User deletion started. userId={}, socialType={}", userId, findUser.getSocialType());

        if (findUser.getSocialType().equals(SocialType.APPLE)) {
            AppleRefreshToken appleRefreshToken = appleRefreshTokenLowService.findByUserId(userId);
            log.info("Apple user deletion detected. userId={}, appleRefreshTokenId={}", userId, appleRefreshToken.getId());
            appleApiConnector.revokeToken(appleRefreshToken.getToken());
            appleRefreshTokenLowService.deleteByUserId(userId);
        }

        surveyLowService.deleteByUserId(userId);
        subscribeLowService.deleteByUserId(userId);
        diaryStoreLowService.deleteByUserId(userId);
        diaryReportLowService.deleteByUserId(userId);
        diaryLikeLowService.deleteByUserId(userId);
        diaryOrderLowService.deleteByUserId(userId);

        diaryService.deleteByUserId(userId);
        jwtService.deleteRefreshTokenByUserId(userId);
        userLowService.deleteByUserId(userId);

        s3Service.deleteS3File(findUser.getProfileImage());
        log.info("User deletion completed. userId={}", userId);
    }

    public UserResponse changeTag(UserTagUpdateRequest userTagUpdateRequest, Long userId) {
        UserEntity findUser = userLowService.findById(userId);

        if (userTagUpdateRequest.tag().equals(findUser.getTag()))
            return new UserResponse(findUser);

        if (userLowService.existsByTag(userTagUpdateRequest.tag())) {
            throw new DuplicateException(ExceptionCode.DUPLICATE_USER_TAG.getDescription());
        }

        findUser.changeTag(userTagUpdateRequest.tag());

        return new UserResponse(findUser);
    }

    @Transactional(readOnly = true)
    public Page<UserSearchResponse> findByTagOrUserName(Long subscriberId, String searchCond, Pageable pageable) {
        Page<UserEntity> findUsers = userLowService.findByTagOrUsername(searchCond, pageable);

        List<Long> userIds = findUsers.stream().map(UserEntity::getId).toList();

        Set<Long> subscribedToIds = subscribeLowService.findBySubscriberIdAndSubscribedToIds(subscriberId, userIds)
                .stream()
                .map(subscribe -> subscribe.getSubscribedTo().getId()).collect(Collectors.toSet());

        return findUsers
                .map(user -> {
                    boolean isMyPick = subscribedToIds.contains(user.getId());
                    return new UserSearchResponse(user, isMyPick);
                });
    }

    public UserResponse changeProfileImage(FileUploadRequest fileUploadRequest, Long userId) {
        UserEntity findUser = userLowService.findById(userId);

        String oldProfileImage = findUser.getProfileImage();

        String fileName = S3Util.extractFileName(fileUploadRequest.presignedUrl());

        findUser.changeProfileImage(fileName);

        temporalLowService.deleteById(fileUploadRequest.id());

        s3Service.deleteS3File(oldProfileImage);

        return new UserResponse(findUser);
    }

    public UserResponse deleteProfileImage(Long userId) {
        UserEntity findUser = userLowService.findById(userId);
        String oldProfileImage = findUser.getProfileImage();
        findUser.setDefaultImage();

        s3Service.deleteS3File(oldProfileImage);

        return new UserResponse(findUser);
    }

    @Transactional(readOnly = true)
    public UserStaticsResponse getUserStatics(Long userId) {
        int killingPartCount = diaryLowService.countByUserId(userId);
        int fanCount = subscribeLowService.countSubscriberBySubscribedToId(userId);
        int pickCount = subscribeLowService.countSubscribedTobySubscriberId(userId);

        return new UserStaticsResponse(fanCount, pickCount, killingPartCount);
    }

    private void joinAppleUser(UserEntity userEntity, OAuth2Response oAuth2Response) {

        AppleOAuth2Rep appleOAuth2Rep = (AppleOAuth2Rep) oAuth2Response;

        AppleTokenResponse appleTokenResponse = appleApiConnector.getAppleRefreshToken(appleOAuth2Rep.authorizationCode());

        String refreshToken = appleTokenResponse.refreshToken();


        appleRefreshTokenLowService.save(new AppleRefreshToken(userEntity, refreshToken));

    }


}
