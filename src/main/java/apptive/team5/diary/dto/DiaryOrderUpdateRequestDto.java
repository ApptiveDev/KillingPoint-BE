package apptive.team5.diary.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DiaryOrderUpdateRequestDto(
        @NotNull(message = "리스트는 null일 수 없습니다.")
        List<Long> diaryIds
) {
}
