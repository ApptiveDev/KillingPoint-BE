package apptive.team5.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        @Size(max = 500, message = "댓글은 500자 이하로 입력해주세요.")
        String text
) {
}
