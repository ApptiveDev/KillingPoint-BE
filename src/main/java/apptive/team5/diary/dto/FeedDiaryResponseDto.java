package apptive.team5.diary.dto;

import apptive.team5.comment.dto.CommentPreviewResponse;
import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryScope;
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
        String profileImageUrl,
        boolean isRecommended,
        String recommendationReason,
        CommentPreviewResponse commentPreview
) implements DiaryResponseDto {
    public static FeedDiaryResponseDto from(DiaryEntity diary, boolean isLiked, boolean isStored, Long likeCount, Long currentUserId, CommentPreviewResponse commentPreview) {
        return from(diary, isLiked, isStored, likeCount, currentUserId, commentPreview, false, null);
    }

    public static FeedDiaryResponseDto from(
            DiaryEntity diary,
            boolean isLiked,
            boolean isStored,
            Long likeCount,
            Long currentUserId,
            CommentPreviewResponse commentPreview,
            boolean isRecommended,
            String recommendationReason
    ) {
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
                S3Util.s3Url + user.getProfileImage(),
                isRecommended,
                recommendationReason,
                commentPreview
        );
    }
}
