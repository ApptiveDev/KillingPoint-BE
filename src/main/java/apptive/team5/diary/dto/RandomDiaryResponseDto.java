package apptive.team5.diary.dto;

import java.util.List;

public record RandomDiaryResponseDto(
        List<FeedDiaryResponseDto> content,
        int pageSize
) {

    public RandomDiaryResponseDto(List<FeedDiaryResponseDto> feedDiaryResponseDtos) {
        this(feedDiaryResponseDtos, feedDiaryResponseDtos.size());

    }
}
