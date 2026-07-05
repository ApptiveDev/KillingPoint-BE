package apptive.team5.recommendation.domain;

import apptive.team5.global.entity.BaseTimeEntity;
import apptive.team5.user.domain.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_artist_preference",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_artist_preference_user_artist",
                        columnNames = {"user_id", "source_artist_id"}
                )
        },
        indexes = {
                @Index(name = "idx_user_artist_preference_user_score", columnList = "user_id, score, updateDateTime")
        }
)
public class UserArtistPreferenceEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "source_artist_id", nullable = false, length = 64)
    private String sourceArtistId;

    @Column(nullable = false)
    private int score;

    public UserArtistPreferenceEntity(UserEntity user, String sourceArtistId, int score) {
        this.user = user;
        this.sourceArtistId = sourceArtistId;
        this.score = score;
    }

    public void adjustScore(int scoreChange) {
        this.score += scoreChange;
    }
}
