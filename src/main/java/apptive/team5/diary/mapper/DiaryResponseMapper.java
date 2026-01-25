package apptive.team5.diary.mapper;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.dto.DiaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DiaryResponseMapper {

    @FunctionalInterface
    public interface DiaryResponseDtoMapper<T extends DiaryResponseDto> {
        T map(DiaryEntity diary, boolean isLiked, boolean isStored, Long likeCount, Long currentUserId);
    }

    public <T extends DiaryResponseDto> Page<T> mapToResponseDto(
            Page<DiaryEntity> diaryPage,
            Set<Long> likedDiaryIds,
            Set<Long> storedDiaryIds,
            Map<Long, Long> likeCountsMap,
            Long currentUserId,
            DiaryResponseDtoMapper<T> mapper
    ) {
        if (diaryPage.isEmpty()) {
            return Page.empty(diaryPage.getPageable());
        }

        return diaryPage.map(diary ->
                mapper.map(
                        diary,
                        likedDiaryIds.contains(diary.getId()),
                        storedDiaryIds.contains(diary.getId()),
                        likeCountsMap.getOrDefault(diary.getId(), 0L),
                        currentUserId
                )
        );
    }

    public <T extends DiaryResponseDto> List<T> mapToResponseDto(
            List<DiaryEntity> diaries,
            Set<Long> likedDiaryIds,
            Set<Long> storedDiaryIds,
            Map<Long, Long> likeCountsMap,
            Long currentUserId,
            DiaryResponseDtoMapper<T> mapper
    ) {
        return diaries.stream()
                .map(diary ->
                        mapper.map(
                                diary,
                                likedDiaryIds.contains(diary.getId()),
                                storedDiaryIds.contains(diary.getId()),
                                likeCountsMap.getOrDefault(diary.getId(), 0L),
                                currentUserId
                        )
                )
                .toList();
    }
}
