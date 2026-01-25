package apptive.team5.diary.dto;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryScope;
import apptive.team5.global.exception.BadRequestException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.global.util.S3Util;
import apptive.team5.user.domain.UserEntity;

import java.time.LocalDateTime;

public record FeedDiaryResponseDto (
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
        Long userId,
        String username,
        String tag,
        String profileImageUrl
) implements DiaryResponseDto {
    public static FeedDiaryResponseDto from(DiaryEntity diary, boolean isLiked, boolean isStored, Long likeCount, Long currentUserId) {
        String contentResponse = diary.getContentForViewer(currentUserId);
        UserEntity user = diary.getUser();

        return new FeedDiaryResponseDto(
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
                likeCount,
                user.getId(),
                user.getUsername(),
                user.getTag(),
                S3Util.s3Url + user.getProfileImage()
        );
    }
}
