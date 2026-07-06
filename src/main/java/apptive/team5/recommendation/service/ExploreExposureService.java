package apptive.team5.recommendation.service;

import apptive.team5.recommendation.domain.UserExploreExposureEntity;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ExploreExposureService {

    private static final int RETENTION_DAYS = 7;

    private final UserLowService userLowService;
    private final UserExploreExposureLowService userExploreExposureLowService;

    @Transactional(readOnly = true)
    public Set<Long> findRecentlyExposedDiaryIds(Long userId) {
        return userExploreExposureLowService.findTop50ByUserId(userId).stream()
                .map(UserExploreExposureEntity::getDiaryId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    public void saveExposures(Long userId, List<Long> diaryIds) {
        if (diaryIds == null || diaryIds.isEmpty()) {
            return;
        }

        UserEntity user = userLowService.getReferenceById(userId);
        List<UserExploreExposureEntity> exposures = diaryIds.stream()
                .distinct()
                .map(diaryId -> new UserExploreExposureEntity(user, diaryId))
                .toList();

        userExploreExposureLowService.saveAll(exposures);
        userExploreExposureLowService.deleteOlderThan(userId, LocalDateTime.now().minusDays(RETENTION_DAYS));
    }
}
