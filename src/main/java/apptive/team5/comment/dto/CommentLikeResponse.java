package apptive.team5.comment.dto;

public record CommentLikeResponse(
        boolean isLiked,
        long likeCount
) {
}
