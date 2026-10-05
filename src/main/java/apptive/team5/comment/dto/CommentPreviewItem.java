package apptive.team5.comment.dto;

import apptive.team5.comment.domain.CommentEntity;
import apptive.team5.global.util.S3Util;
import apptive.team5.user.domain.UserEntity;

public record CommentPreviewItem(
        Long commentId,
        Long userId,
        String profileImageUrl,
        String content
) {

    private static final int PREVIEW_CONTENT_LENGTH = 30;

    public static CommentPreviewItem from(CommentEntity comment) {
        UserEntity user = comment.getUser();
        String content = comment.getContent();

        return new CommentPreviewItem(
                comment.getId(),
                user.getId(),
                S3Util.s3Url + user.getProfileImage(),
                content.length() > PREVIEW_CONTENT_LENGTH ? content.substring(0, PREVIEW_CONTENT_LENGTH) : content
        );
    }
}
