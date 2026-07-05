package apptive.team5.diary.dto;

import apptive.team5.recommendation.domain.MusicMetadataSourceType;

public record DiaryMusicMetadataRequest(
        MusicMetadataSourceType sourceType,
        String trackId,
        String artistId,
        String primaryGenreName
) {
    public boolean hasTrackId() {
        return sourceType != null && trackId != null && !trackId.isBlank();
    }
}
