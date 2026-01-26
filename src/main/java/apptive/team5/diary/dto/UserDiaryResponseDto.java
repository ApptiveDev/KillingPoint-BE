package apptive.team5.diary.dto;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryScope;
import apptive.team5.global.exception.BadRequestException;
import apptive.team5.global.exception.ExceptionCode;

import java.time.LocalDateTime;

public record UserDiaryResponseDto(
        Long diaryId,
        String artist,
        String musicTitle,
        String albumImageUrl,
        String content,
        String videoUrl,
        DiaryScope scope,
        String duration,
        String totalDuration,
        String start,
        String end,
        LocalDateTime createDate,
        LocalDateTime updateDate,
        boolean isLiked,
        boolean isStored,
        Long likeCount
) implements DiaryResponseDto {
    public static UserDiaryResponseDto from(DiaryEntity diary, boolean isLiked, boolean isStored, Long likeCount, Long currentUserId) {
        String contentResponse = diary.getContentForViewer(currentUserId);

        return new UserDiaryResponseDto(
                diary.getId(),
                diary.getArtist(),
                diary.getMusicTitle(),
                diary.getAlbumImageUrl(),
                contentResponse,
                diary.getVideoUrl(),
                diary.getScope(),
                diary.getDuration(),
                diary.getTotalDuration(),
                diary.getStart(),
                diary.getEnd(),
                diary.getCreateDateTime(),
                diary.getUpdateDateTime(),
                isLiked,
                isStored,
                likeCount
        );
    }
}
