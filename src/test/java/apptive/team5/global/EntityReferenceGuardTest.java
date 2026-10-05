package apptive.team5.global;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.user.domain.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EntityReferenceGuardTest {

    private static final Set<String> DIARY_REFERENCING_ENTITIES = Set.of(
            "DiaryLikeEntity",
            "DiaryMemoEntity",
            "DiaryReportEntity",
            "CommentEntity"
    );

    private static final Set<String> USER_REFERENCING_ENTITIES = Set.of(
            "DiaryEntity",
            "DiaryLikeEntity",
            "DiaryReportEntity",
            "DiaryOrderEntity",
            "DiaryStoreEntity",
            "Subscribe",
            "UserBlock",
            "UserInitSettingEntity",
            "UserPolicyAgreementEntity",
            "SurveyEntity",
            "Alarm",
            "DeviceToken",
            "RefreshToken",
            "AppleRefreshToken",
            "UserGenrePreferenceEntity",
            "UserArtistPreferenceEntity",
            "UserExploreExposureEntity",
            "CommentEntity",
            "CommentLikeEntity",
            "CommentReportEntity"
    );

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("DiaryEntity 를 FK 로 참조하는 엔티티가 늘어나면 다이어리 삭제 cascade 와 DiaryDeletionIntegrationTest 를 함께 갱신해야 한다")
    void diaryReferencingEntitiesAreKnown() {
        assertThat(entitiesReferencing(DiaryEntity.class))
                .containsExactlyInAnyOrderElementsOf(DIARY_REFERENCING_ENTITIES);
    }

    @Test
    @DisplayName("UserEntity 를 FK 로 참조하는 엔티티가 늘어나면 UserService.deleteUser 와 UserWithdrawalIntegrationTest 를 함께 갱신해야 한다")
    void userReferencingEntitiesAreKnown() {
        assertThat(entitiesReferencing(UserEntity.class))
                .containsExactlyInAnyOrderElementsOf(USER_REFERENCING_ENTITIES);
    }

    private Set<String> entitiesReferencing(Class<?> target) {
        return em.getMetamodel().getEntities().stream()
                .filter(entity -> entity.getAttributes().stream()
                        .anyMatch(attribute -> attribute.getJavaType().equals(target)))
                .map(EntityType::getName)
                .collect(Collectors.toSet());
    }
}
