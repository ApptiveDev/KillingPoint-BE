package apptive.team5.diary.domain;

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

    public DiaryStoreEntity(UserEntity user, DiaryEntity diary) {
        this.user = user;
        this.diaryId = diary.getId();

        this.musicTitle = diary.getMusicTitle();
        this.artist = diary.getArtist();
        this.albumImageUrl = diary.getAlbumImageUrl();
        this.videoUrl = diary.getVideoUrl();
        this.content = diary.getContentForViewer(user.getId());
        this.scope = diary.getScope();
        this.duration = diary.getDuration();
        this.totalDuration = diary.getTotalDuration();
        this.start = diary.getStart();
        this.end = diary.getEnd();

        UserEntity author = diary.getUser();
        this.originalAuthorId = author.getId();
        this.originalAuthorName = author.getUsername();
        this.originalAuthorTag = author.getTag();
        this.originalAuthorProfileImage = author.getProfileImage();
    }
}
