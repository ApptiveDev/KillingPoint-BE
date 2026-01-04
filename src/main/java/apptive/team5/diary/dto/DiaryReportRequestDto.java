package apptive.team5.diary.dto;

import jakarta.validation.constraints.Size;

public record DiaryReportRequestDto(
        @Size(min = 1, max = 200, message = "신고 내용은 1자 이상 200자 이하입니다.")
        String content
) {
}
