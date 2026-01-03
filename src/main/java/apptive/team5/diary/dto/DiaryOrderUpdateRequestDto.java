package apptive.team5.diary.dto;

import java.util.List;

public record DiaryOrderUpdateRequestDto(
        List<Long> diaryIds
) {
}
