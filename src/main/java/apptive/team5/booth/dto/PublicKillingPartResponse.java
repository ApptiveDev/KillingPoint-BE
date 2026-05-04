package apptive.team5.booth.dto;

import apptive.team5.diary.domain.DiaryEntity;

public record PublicKillingPartResponse(
        Long diaryId,
        String artist,
        String musicTitle,
        String albumImageUrl,
        String videoUrl,
        String totalDuration,
        String start,
        String end
) {
    public static PublicKillingPartResponse from(DiaryEntity diary) {
        return new PublicKillingPartResponse(
                diary.getId(),
                diary.getArtist(),
                diary.getMusicTitle(),
                diary.getAlbumImageUrl(),
                diary.getVideoUrl(),
                diary.getTotalDuration(),
                diary.getStart(),
                diary.getEnd()
        );
    }
}
