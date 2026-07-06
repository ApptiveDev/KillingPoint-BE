package apptive.team5.recommendation.service;

import apptive.team5.recommendation.domain.UserArtistPreferenceEntity;
import apptive.team5.recommendation.repository.UserArtistPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserArtistPreferenceLowService {

    private final UserArtistPreferenceRepository userArtistPreferenceRepository;

    public UserArtistPreferenceEntity save(UserArtistPreferenceEntity preference) {
        return userArtistPreferenceRepository.save(preference);
    }

    public void delete(UserArtistPreferenceEntity preference) {
        userArtistPreferenceRepository.delete(preference);
    }

    @Transactional(readOnly = true)
    public Optional<UserArtistPreferenceEntity> findByUserIdAndSourceArtistId(Long userId, String sourceArtistId) {
        return userArtistPreferenceRepository.findByUser_IdAndSourceArtistId(userId, sourceArtistId);
    }

    @Transactional(readOnly = true)
    public List<UserArtistPreferenceEntity> findTop5ByUserId(Long userId) {
        return userArtistPreferenceRepository.findTop5ByUser_IdOrderByScoreDescUpdateDateTimeDesc(userId);
    }
}
