package apptive.team5.comment.dto;

import apptive.team5.comment.domain.CommentStatus;

import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        Long diaryId,
        Long parentCommentId,
        CommentStatus status,
        boolean isBlocked,
        String text,
        CommentAuthorResponse author,
        boolean isMine,
        boolean isLiked,
        long likeCount,
        long replyCount,
        LocalDateTime createDate,
        boolean isEdited
) {
}
