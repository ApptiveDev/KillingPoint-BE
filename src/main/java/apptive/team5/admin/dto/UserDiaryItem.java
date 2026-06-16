package apptive.team5.admin.dto;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryScope;

public record UserDiaryItem(
        Long diaryId,
        String displayId,
        String musicTitle,
        String artist,
        String scopeLabel,
        long reportCount,
        String reportLabel,
        String reportStatusClass,
        String content
) {

    public static UserDiaryItem from(DiaryEntity diary, long reportCount) {
        return new UserDiaryItem(
                diary.getId(),
                "KP-" + diary.getId(),
                diary.getMusicTitle(),
                diary.getArtist(),
                scopeLabel(diary.getScope()),
                reportCount,
                "신고-" + reportCount,
                reportCount > 0 ? "reported" : "",
                diary.getContent()
        );
    }

    private static String scopeLabel(DiaryScope scope) {
        return switch (scope) {
            case PUBLIC -> "전체공개";
            case KILLING_PART -> "킬링파트만 공개";
            case PRIVATE -> "비공개";
        };
    }
}
