package apptive.team5.diary.dto;

import java.util.Set;

public record AiDiaryReportResponseDto(
        Set<Long> diaryIds
) {
}
