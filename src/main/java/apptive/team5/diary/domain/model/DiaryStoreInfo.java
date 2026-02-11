package apptive.team5.diary.domain.model;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.user.domain.UserEntity;

public record DiaryStoreInfo(
        Long diaryId,
        MusicBasicInfo musicBasicInfo,
        DiaryBasicInfo diaryBasicInfo,
        MusicPlayInfo musicPlayInfo,
        StoredAuthorInfo authorInfo
) {
    public static DiaryStoreInfo from(DiaryEntity diary, UserEntity viewer) {
        String filteredContent = diary.getContentForViewer(viewer.getId());

        return new DiaryStoreInfo(
                diary.getId(),
                new MusicBasicInfo(
                        diary.getMusicTitle(),
                        diary.getArtist(),
                        diary.getAlbumImageUrl(),
                        diary.getVideoUrl()
                ),
                new DiaryBasicInfo(
                        filteredContent,
                        diary.getScope()
                ),
                new MusicPlayInfo(
                        diary.getDuration(),
                        diary.getTotalDuration(),
                        diary.getStart(),
                        diary.getEnd()
                ),
                new StoredAuthorInfo(
                        diary.getUser().getId(),
                        diary.getUser().getUsername(),
                        diary.getUser().getTag(),
                        diary.getUser().getProfileImage()
                )
        );
    }
}
