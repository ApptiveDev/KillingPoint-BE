package apptive.team5.comment.dto;

import apptive.team5.global.util.S3Util;
import apptive.team5.user.domain.UserEntity;

public record MentionDisplayResponse(
        String username,
        String tag,
        String profileImageUrl
) {

    public static MentionDisplayResponse from(UserEntity user) {
        return new MentionDisplayResponse(
                user.getUsername(),
                user.getTag(),
                S3Util.s3Url + user.getProfileImage()
        );
    }
}
