package apptive.team5.comment.dto;

public record CommentReplyCountDto(
        Long parentCommentId,
        Long replyCount
) {
}
