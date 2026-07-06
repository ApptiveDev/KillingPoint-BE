package apptive.team5.recommendation.domain;

import apptive.team5.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "music_metadata",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_music_metadata_source_track",
                        columnNames = {"source_type", "source_track_id"}
                )
        },
        indexes = {
                @Index(name = "idx_music_metadata_genre", columnList = "primary_genre_name_raw"),
                @Index(name = "idx_music_metadata_artist", columnList = "source_artist_id")
        }
)
public class MusicMetadataEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 20)
    private MusicMetadataSourceType sourceType;

    @Column(name = "source_track_id", length = 64)
    private String sourceTrackId;

    @Column(name = "source_artist_id", length = 64)
    private String sourceArtistId;

    @Column(name = "primary_genre_name_raw", length = 128)
    private String primaryGenreNameRaw;

    public MusicMetadataEntity(
            MusicMetadataSourceType sourceType,
            String sourceTrackId,
            String sourceArtistId,
            String primaryGenreNameRaw
    ) {
        this.sourceType = sourceType;
        this.sourceTrackId = sourceTrackId;
        this.sourceArtistId = sourceArtistId;
        this.primaryGenreNameRaw = primaryGenreNameRaw;
    }
}
