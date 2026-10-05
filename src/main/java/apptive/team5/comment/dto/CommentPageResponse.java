package apptive.team5.comment.dto;

import org.springframework.data.domain.Page;

public record CommentPageResponse(
        long commentCount,
        Page<CommentResponse> comments
) {
}
