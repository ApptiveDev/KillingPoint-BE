package apptive.team5.recommendation.service;

import apptive.team5.recommendation.domain.UserGenrePreferenceEntity;
import apptive.team5.recommendation.repository.UserGenrePreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserGenrePreferenceLowService {

    private final UserGenrePreferenceRepository userGenrePreferenceRepository;

    public UserGenrePreferenceEntity save(UserGenrePreferenceEntity preference) {
        return userGenrePreferenceRepository.save(preference);
    }

    public void delete(UserGenrePreferenceEntity preference) {
        userGenrePreferenceRepository.delete(preference);
    }

    @Transactional(readOnly = true)
    public Optional<UserGenrePreferenceEntity> findByUserIdAndGenreNameRaw(Long userId, String genreNameRaw) {
        return userGenrePreferenceRepository.findByUser_IdAndGenreNameRaw(userId, genreNameRaw);
    }

    @Transactional(readOnly = true)
    public List<UserGenrePreferenceEntity> findTop3ByUserId(Long userId) {
        return userGenrePreferenceRepository.findTop3ByUser_IdOrderByScoreDescUpdateDateTimeDesc(userId);
    }
}
