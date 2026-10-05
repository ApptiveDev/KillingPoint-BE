package apptive.team5.comment.dto;

import java.util.List;

public record CommentAttachmentResponse(
        List<MentionReferenceResponse> references
) {

    private static final CommentAttachmentResponse EMPTY = new CommentAttachmentResponse(List.of());

    public static CommentAttachmentResponse empty() {
        return EMPTY;
    }
}
