package apptive.team5.comment.dto;

import apptive.team5.comment.domain.CommentReportEntity;

public record CommentReportResponseDto(
        Long id,
        String reason,
        String reportContent,
        Long userId,
        Long commentId
) {

    public CommentReportResponseDto(CommentReportEntity commentReport) {
        this(
                commentReport.getId(),
                commentReport.getReason(),
                commentReport.getReportContent(),
                commentReport.getUser().getId(),
                commentReport.getComment().getId()
        );
    }
}
