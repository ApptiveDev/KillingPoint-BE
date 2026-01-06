package apptive.team5.diary.dto;

import apptive.team5.diary.domain.DiaryReportEntity;

public record AiDiaryReportRequestDto(
        Long diaryId,
        String reason,
        String reportContent

) {

    public AiDiaryReportRequestDto(DiaryReportEntity diaryReport) {
        this(diaryReport.getDiary().getId(), diaryReport.getReason(), diaryReport.getReportContent());
    }

}
