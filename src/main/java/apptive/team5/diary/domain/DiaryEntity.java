package apptive.team5.diary.domain;

import apptive.team5.diary.domain.model.DiaryBasicInfo;
import apptive.team5.diary.domain.model.DiaryInfo;
import apptive.team5.diary.domain.model.MusicPlayInfo;
import apptive.team5.global.entity.BaseTimeEntity;
import apptive.team5.global.exception.BadRequestException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.diary.domain.model.MusicBasicInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.access.AccessDeniedException;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "diary_entity",
        indexes = {
                @Index(name = "idx_diary_user_created", columnList = "user_id, createDateTime"),
                @Index(name ="idx_user_id_diary_id", columnList = "user_id, diary_id")
        }
)
public class DiaryEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id")
    private Long id;

    @Column(nullable = false)
    private String musicTitle;
    @Column(nullable = false)
    private String artist;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String albumImageUrl;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String videoUrl;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiaryScope scope;

    @Column(nullable = false)
    private String duration;
    @Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT '0'")
    private String totalDuration;
    @Column(nullable = false)
    private String start;
    @Column(nullable = false)
    private String end;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private UserEntity user;

    private static final String HIDDEN_CONTENT_DEFAULT_MESSAGE = "비공개 일기입니다.";

    public DiaryEntity(
            DiaryInfo info,
            UserEntity user
    ) {
        this.user = user;
        update(info);
    }

    public String getContentForViewer(Long viewerId) {
        if (isMyDiary(viewerId)) {
            return this.content;
        }

        if (this.scope == DiaryScope.PRIVATE) {
            throw new BadRequestException(ExceptionCode.ACCESS_DENIED_DIARY.getDescription());
        }

        if (this.scope == DiaryScope.KILLING_PART) {
            return HIDDEN_CONTENT_DEFAULT_MESSAGE;
        }

        return this.content;
    }

    public void validateOwner(UserEntity user) {
        if (!isOwner(user.getId())) {
            throw new AccessDeniedException(ExceptionCode.ACCESS_DENIED_DIARY.getDescription());
        }
    }

    public boolean isMyDiary(Long userId) {
        return isOwner(userId);
    }

    private boolean isOwner(Long userId) {
        return this.user.getId().equals(userId);
    }

    public void update(DiaryInfo info) {
        updateMusicBaseInfo(info.musicBasicInfo());
        updateDiaryBasicInfo(info.diaryBasicInfo());
        updateMusicPlayInfo(info.musicPlayInfo());
    }

    public void updateMusicBaseInfo(MusicBasicInfo musicBasicInfo) {
        updateMusicTitle(musicBasicInfo.musicTitle());
        updateArtist(musicBasicInfo.artist());
        updateAlbumImageUrl(musicBasicInfo.albumImageUrl());
        updateVideoUrl(musicBasicInfo.videoUrl());
    }

    public void updateDiaryBasicInfo(DiaryBasicInfo diaryBasicInfo) {
        updateContent(diaryBasicInfo.content());
        updateScope(diaryBasicInfo.scope());
    }

    public void updateMusicPlayInfo(MusicPlayInfo musicPlayInfo) {
        updateDuration(musicPlayInfo.duration());
        updateTotalDuration(musicPlayInfo.totalDuration());
        updateStart(musicPlayInfo.start());
        updateEnd(musicPlayInfo.end());
    }

    private void updateMusicTitle(String musicTitle) {
        if (musicTitle != null && !musicTitle.isBlank()) {
            this.musicTitle = musicTitle;
        }
    }

    private void updateArtist(String artist) {
        if (artist != null && !artist.isBlank()) {
            this.artist = artist;
        }
    }

    private void updateAlbumImageUrl(String albumImageUrl) {
        if (albumImageUrl != null && !albumImageUrl.isBlank()) {
            this.albumImageUrl = albumImageUrl;
        }
    }

    private void updateVideoUrl(String videoUrl) {
        if (videoUrl != null && !videoUrl.isBlank()) {
            this.videoUrl = videoUrl;
        }
    }

    private void updateContent(String content) {
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
    }

    private void updateScope(DiaryScope scope) {
        if (scope != null) {
            this.scope = scope;
        }
    }

    private void updateDuration(String duration) {
        if (duration != null && !duration.isBlank()) {
            this.duration = duration;
        }
    }

    private void updateTotalDuration(String totalDuration) {
        if (totalDuration != null && !totalDuration.isBlank()) {
            this.totalDuration = totalDuration;
        }
    }

    private void updateStart(String start) {
        if (start != null && !start.isBlank()) {
            this.start = start;
        }
    }

    private void updateEnd(String end) {
        if (end != null && !end.isBlank()) {
            this.end = end;
        }
    }
}
