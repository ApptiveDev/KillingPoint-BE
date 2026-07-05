package apptive.team5.diary.dto;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryScope;
import apptive.team5.diary.domain.model.DiaryBasicInfo;
import apptive.team5.diary.domain.model.DiaryInfo;
import apptive.team5.diary.domain.model.MusicBasicInfo;
import apptive.team5.diary.domain.model.MusicPlayInfo;
import apptive.team5.user.domain.UserEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DiaryCreateRequest(
        @NotBlank(message = "아티스트는 필수 입력입니다.")
        String artist,
        @NotBlank(message = "음악 제목은 필수 입력입니다.")
        String musicTitle,
        @NotBlank(message = "앨범 이미지 URL은 필수 입력입니다.")
        String albumImageUrl,
        @NotBlank(message = "video URL은 필수 입력입니다.")
        String videoUrl,
        @NotBlank(message = "내용을 입력해주세요")
        String content,
        @NotNull(message = "공개 범위는 필수 입력입니다.")
        DiaryScope scope,
        @NotBlank(message = "킬링파트 길이는 필수 입력입니다.")
        String duration,
        @NotBlank(message = "영상 길이는 필수 입력입니다.")
        String totalDuration,
        @NotBlank(message = "킬링파트 시작 시간은 필수 입력입니다.")
        String start,
        @NotBlank(message = "킬링파트 종료 시간은 필수 입력입니다.")
        String end,
        DiaryMusicMetadataRequest musicMetadata
) {
    public DiaryEntity toEntity(UserEntity user) {
        DiaryInfo diaryInfo = new DiaryInfo(
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

        return new DiaryEntity(
                diaryInfo,
                user
        );
    }
}
