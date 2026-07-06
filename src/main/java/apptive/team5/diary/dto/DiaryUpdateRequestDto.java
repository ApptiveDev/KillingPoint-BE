package apptive.team5.diary.dto;

import apptive.team5.diary.domain.DiaryScope;
import apptive.team5.diary.domain.model.DiaryBasicInfo;
import apptive.team5.diary.domain.model.DiaryInfo;
import apptive.team5.diary.domain.model.MusicBasicInfo;
import apptive.team5.diary.domain.model.MusicPlayInfo;

public record DiaryUpdateRequestDto(
        String artist,
        String musicTitle,
        String albumImageUrl,
        String videoUrl,
        String content,
        DiaryScope scope,
        String duration,
        String totalDuration,
        String start,
        String end,
        DiaryMusicMetadataRequest musicMetadata
) {
    public DiaryInfo toDomainInfo() {
        return new DiaryInfo(
                new MusicBasicInfo(
                        musicTitle,
                        artist,
                        albumImageUrl,
                        videoUrl
                ),
                new DiaryBasicInfo(
                        content,
                        scope
                ),
                new MusicPlayInfo(
                        duration,
                        totalDuration,
                        start,
                        end
                )
        );
    }
}
