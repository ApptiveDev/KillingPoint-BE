package apptive.team5.diary.domain;

import apptive.team5.diary.domain.model.*;
import apptive.team5.global.entity.BaseTimeEntity;
import apptive.team5.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "diary_store",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_diary_store_user_diary",
                        columnNames = {"user_id", "diary_id"}
                )
        }
)
public class DiaryStoreEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_diary_store_user_id_ref_user_id")
    )
    private UserEntity user;

    @Column(name = "diary_id", nullable = false)
    private Long diaryId;

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

    @Column(nullable = false)
    private Long originalAuthorId;

    @Column(nullable = false)
    private String originalAuthorName;

    @Column(nullable = false)
    private String originalAuthorTag;

    @Column(nullable = false)
    private String originalAuthorProfileImage;

    public DiaryStoreEntity(UserEntity user, DiaryStoreInfo info) {
        this.user = user;
        this.diaryId = info.diaryId();
        saveInfo(info);
    }

    private void saveInfo(DiaryStoreInfo info) {
        saveMusicBaseInfo(info.musicBasicInfo());
        saveDiaryBasicInfo(info.diaryBasicInfo());
        saveMusicPlayInfo(info.musicPlayInfo());
        saveAuthorInfo(info.authorInfo());
    }

    private void saveMusicBaseInfo(MusicBasicInfo info) {
        this.musicTitle = info.musicTitle();
        this.artist = info.artist();
        this.albumImageUrl = info.albumImageUrl();
        this.videoUrl = info.videoUrl();
    }

    private void saveDiaryBasicInfo(DiaryBasicInfo info) {
        this.content = info.content();
        this.scope = info.scope();
    }

    private void saveMusicPlayInfo(MusicPlayInfo info) {
        this.duration = info.duration();
        this.totalDuration = info.totalDuration();
        this.start = info.start();
        this.end = info.end();
    }

    private void saveAuthorInfo(StoredAuthorInfo info) {
        this.originalAuthorId = info.id();
        this.originalAuthorName = info.name();
        this.originalAuthorTag = info.tag();
        this.originalAuthorProfileImage = info.profileImage();
    }
}
