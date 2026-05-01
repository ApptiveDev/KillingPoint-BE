package apptive.team5.user.dto;

import apptive.team5.global.util.S3Util;
import apptive.team5.user.domain.SocialType;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.domain.UserRoleType;

public record UserResponse(
        Long userId,
        String username,
        String tag,
        String identifier,
        String profileImageUrl,
        boolean alarmEnabled,
        UserRoleType userRoleType,
        SocialType socialType
) {

    public UserResponse(UserEntity userEntity) {
        this(   userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getTag(),
                userEntity.getIdentifier(),
                S3Util.s3Url + userEntity.getProfileImage(),
                userEntity.isAlarmEnabled(),
                userEntity.getRoleType(),
                userEntity.getSocialType());
    }
}
