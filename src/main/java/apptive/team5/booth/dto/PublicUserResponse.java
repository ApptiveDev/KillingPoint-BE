package apptive.team5.booth.dto;

import apptive.team5.global.util.S3Util;
import apptive.team5.user.domain.UserEntity;

public record PublicUserResponse(
        Long userId,
        String username,
        String tag,
        String profileImageUrl
) {
    public static PublicUserResponse from(UserEntity user) {
        return new PublicUserResponse(
                user.getId(),
                user.getUsername(),
                user.getTag(),
                S3Util.s3Url + user.getProfileImage()
        );
    }
}
