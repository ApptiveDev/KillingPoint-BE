package apptive.team5.comment.dto;

import apptive.team5.global.util.S3Util;
import apptive.team5.user.domain.UserEntity;

public record CommentAuthorResponse(
        Long userId,
        String username,
        String tag,
        String profileImageUrl
) {

    public static CommentAuthorResponse from(UserEntity user) {
        return new CommentAuthorResponse(
                user.getId(),
                user.getUsername(),
                user.getTag(),
                S3Util.s3Url + user.getProfileImage()
        );
    }

    public static CommentAuthorResponse blocked(UserEntity user) {
        return new CommentAuthorResponse(user.getId(), null, null, null);
    }
}
