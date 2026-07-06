package apptive.team5.recommendation.domain;

import apptive.team5.global.entity.BaseTimeEntity;
import apptive.team5.user.domain.UserEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_explore_exposure",
        indexes = {
                @Index(name = "idx_user_explore_exposure_user_created", columnList = "user_id, createDateTime"),
                @Index(name = "idx_user_explore_exposure_user_diary", columnList = "user_id, diary_id")
        }
)
public class UserExploreExposureEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @jakarta.persistence.Column(name = "diary_id", nullable = false)
    private Long diaryId;

    public UserExploreExposureEntity(UserEntity user, Long diaryId) {
        this.user = user;
        this.diaryId = diaryId;
    }
}
