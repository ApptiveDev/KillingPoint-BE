package apptive.team5.diary.dto;

import apptive.team5.diary.domain.DiaryReportEntity;

public record DiaryReportResponseDto(
        Long id,
        String reason,
        String reportContent,
        Long userId
) {

    public DiaryReportResponseDto(DiaryReportEntity diaryReportEntity) {
        this(
                diaryReportEntity.getId(), diaryReportEntity.getReason(),
                diaryReportEntity.getReportContent(), diaryReportEntity.getUser().getId()
        );
    }

}
