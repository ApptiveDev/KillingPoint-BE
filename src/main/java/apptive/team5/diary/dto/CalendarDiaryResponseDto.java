package apptive.team5.diary.dto;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryScope;

import java.time.LocalDateTime;

public record CalendarDiaryResponseDto(
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
            LocalDateTime updateDate
    ) implements DiaryResponseDto {
    public static CalendarDiaryResponseDto from(DiaryEntity diary) {
        return new CalendarDiaryResponseDto(
                diary.getId(),
                diary.getArtist(),
                diary.getMusicTitle(),
                diary.getAlbumImageUrl(),
                diary.getContent(),
                diary.getVideoUrl(),
                diary.getScope(),
                diary.getDuration(),
                diary.getTotalDuration(),
                diary.getStart(),
                diary.getEnd(),
                diary.getCreateDateTime(),
                diary.getUpdateDateTime()
        );
    }

}

