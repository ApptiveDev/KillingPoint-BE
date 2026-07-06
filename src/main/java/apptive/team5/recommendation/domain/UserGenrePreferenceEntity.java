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
        name = "user_genre_preference",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_genre_preference_user_genre",
                        columnNames = {"user_id", "genre_name_raw"}
                )
        },
        indexes = {
                @Index(name = "idx_user_genre_preference_user_score", columnList = "user_id, score, updateDateTime")
        }
)
public class UserGenrePreferenceEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "genre_name_raw", nullable = false, length = 128)
    private String genreNameRaw;

    @Column(nullable = false)
    private int score;

    public UserGenrePreferenceEntity(UserEntity user, String genreNameRaw, int score) {
        this.user = user;
        this.genreNameRaw = genreNameRaw;
        this.score = score;
    }

    public void adjustScore(int scoreChange) {
        this.score += scoreChange;
    }
}
