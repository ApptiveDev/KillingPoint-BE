package apptive.team5.diary.dto;

import apptive.team5.diary.domain.DiaryStoreEntity;

public record StoredDiaryResponseDto(
        Long diaryId,
        String musicTitle,
        String artist,
        String albumImageUrl,
        String videoUrl,

        String duration,
        String totalDuration,
        String start,
        String end
) {
    public static StoredDiaryResponseDto from(DiaryStoreEntity entity) {
        return new StoredDiaryResponseDto(
                entity.getDiaryId(),
                entity.getMusicTitle(),
                entity.getArtist(),
                entity.getAlbumImageUrl(),
                entity.getVideoUrl(),
                entity.getDuration(),
                entity.getTotalDuration(),
                entity.getStart(),
                entity.getEnd()
        );
    }
}
