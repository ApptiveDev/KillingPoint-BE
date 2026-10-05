package apptive.team5.comment.dto;

import apptive.team5.global.util.S3Util;
import apptive.team5.user.domain.UserEntity;

public record MentionCandidateResponse(
        Long userId,
        String username,
        String tag,
        String profileImageUrl
) {

    public static MentionCandidateResponse from(UserEntity user) {
        return new MentionCandidateResponse(
                user.getId(),
                user.getUsername(),
                user.getTag(),
                S3Util.s3Url + user.getProfileImage()
        );
    }
}
