package apptive.team5.comment.dto;

import java.util.List;

public record CommentPreviewResponse(
        long commentCount,
        List<CommentPreviewItem> comments
) {

    private static final CommentPreviewResponse EMPTY = new CommentPreviewResponse(0L, List.of());

    public static CommentPreviewResponse empty() {
        return EMPTY;
    }
}
