package apptive.team5.admin.dto;

import apptive.team5.diary.domain.DiaryMemoEntity;

public record AdminMemoItem(
        Long memoId,
        Long diaryId,
        String displayId,
        String musicTitle,
        String artist,
        String username,
        Long userId,
        String albumImageUrl,
        String memoContent,
        String diaryContent
) {

    public static AdminMemoItem from(DiaryMemoEntity memo) {
        var diary = memo.getDiary();

        return new AdminMemoItem(
                memo.getId(),
                diary.getId(),
                "KP-" + diary.getId(),
                diary.getMusicTitle(),
                diary.getArtist(),
                diary.getUser().getUsername(),
                diary.getUser().getId(),
                diary.getAlbumImageUrl(),
                memo.getContent(),
                diary.getContent()
        );
    }
}
