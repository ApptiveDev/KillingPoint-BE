package apptive.team5.diary.dto;


import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryScope;
import java.time.LocalDateTime;

public record MyDiaryResponseDto(
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
        Long likeCount,
        Long userId
) implements DiaryResponseDto {
    public static MyDiaryResponseDto from(DiaryEntity diary, boolean isLiked, boolean isStored, Long likeCount, Long userId) {
        return new MyDiaryResponseDto(
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
                diary.getUpdateDateTime(),
                isLiked,
                isStored,
                likeCount,
                userId
        );
    }
}
