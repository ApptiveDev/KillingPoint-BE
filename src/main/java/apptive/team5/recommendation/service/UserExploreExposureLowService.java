package apptive.team5.recommendation.service;

import apptive.team5.recommendation.domain.UserExploreExposureEntity;
import apptive.team5.recommendation.repository.UserExploreExposureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserExploreExposureLowService {

    private final UserExploreExposureRepository userExploreExposureRepository;

    public List<UserExploreExposureEntity> saveAll(List<UserExploreExposureEntity> exposures) {
        return userExploreExposureRepository.saveAll(exposures);
    }

    @Transactional(readOnly = true)
    public List<UserExploreExposureEntity> findRecentByUserId(Long userId, int limit) {
        return userExploreExposureRepository.findByUser_IdOrderByCreateDateTimeDesc(userId, PageRequest.of(0, limit));
    }

    public void deleteOlderThan(Long userId, LocalDateTime cutoff) {
        userExploreExposureRepository.deleteByUser_IdAndCreateDateTimeBefore(userId, cutoff);
    }
}
