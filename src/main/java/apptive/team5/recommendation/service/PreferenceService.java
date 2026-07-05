package apptive.team5.recommendation.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.recommendation.domain.MusicMetadataEntity;
import apptive.team5.recommendation.domain.UserArtistPreferenceEntity;
import apptive.team5.recommendation.domain.UserGenrePreferenceEntity;
import apptive.team5.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PreferenceService {

    private static final int DIARY_CREATE_SCORE = 5;
    private static final int DIARY_STORE_SCORE = 4;
    private static final int DIARY_LIKE_SCORE = 2;

    private final UserGenrePreferenceLowService userGenrePreferenceLowService;
    private final UserArtistPreferenceLowService userArtistPreferenceLowService;

    public void reflectDiaryCreated(UserEntity user, DiaryEntity diary) {
        applyPreference(user, diary, DIARY_CREATE_SCORE);
    }

    public void reflectDiaryDeleted(UserEntity user, DiaryEntity diary) {
        applyPreference(user, diary, -DIARY_CREATE_SCORE);
    }

    public void reflectDiaryStored(UserEntity user, DiaryEntity diary) {
        applyPreference(user, diary, DIARY_STORE_SCORE);
    }

    public void reflectDiaryStoreRemoved(UserEntity user, DiaryEntity diary) {
        applyPreference(user, diary, -DIARY_STORE_SCORE);
    }

    public void reflectDiaryLiked(UserEntity user, DiaryEntity diary) {
        applyPreference(user, diary, DIARY_LIKE_SCORE);
    }

    public void reflectDiaryLikeRemoved(UserEntity user, DiaryEntity diary) {
        applyPreference(user, diary, -DIARY_LIKE_SCORE);
    }

    private void applyPreference(UserEntity user, DiaryEntity diary, int scoreChange) {
        if (user == null || diary == null) {
            return;
        }

        MusicMetadataEntity musicMetadata = diary.getMusicMetadata();
        if (musicMetadata == null) {
            return;
        }

        applyGenrePreference(user, musicMetadata, scoreChange);
        applyArtistPreference(user, musicMetadata, scoreChange);
    }

    private void applyGenrePreference(UserEntity user, MusicMetadataEntity musicMetadata, int scoreChange) {
        String genreNameRaw = musicMetadata.getPrimaryGenreNameRaw();
        if (genreNameRaw == null || genreNameRaw.isBlank()) {
            return;
        }

        userGenrePreferenceLowService.findByUserIdAndGenreNameRaw(user.getId(), genreNameRaw)
                .ifPresentOrElse(
                        preference -> applyGenrePreference(preference, scoreChange),
                        () -> createGenrePreference(user, genreNameRaw, scoreChange)
                );
    }

    private void applyArtistPreference(UserEntity user, MusicMetadataEntity musicMetadata, int scoreChange) {
        String sourceArtistId = musicMetadata.getSourceArtistId();
        if (sourceArtistId == null || sourceArtistId.isBlank()) {
            return;
        }

        userArtistPreferenceLowService.findByUserIdAndSourceArtistId(user.getId(), sourceArtistId)
                .ifPresentOrElse(
                        preference -> applyArtistPreference(preference, scoreChange),
                        () -> createArtistPreference(user, sourceArtistId, scoreChange)
                );
    }

    private void applyGenrePreference(UserGenrePreferenceEntity preference, int scoreChange) {
        preference.adjustScore(scoreChange);
        if (preference.getScore() <= 0) {
            userGenrePreferenceLowService.delete(preference);
        }
    }

    private void applyArtistPreference(UserArtistPreferenceEntity preference, int scoreChange) {
        preference.adjustScore(scoreChange);
        if (preference.getScore() <= 0) {
            userArtistPreferenceLowService.delete(preference);
        }
    }

    private void createGenrePreference(UserEntity user, String genreNameRaw, int scoreChange) {
        if (scoreChange <= 0) {
            return;
        }

        userGenrePreferenceLowService.save(new UserGenrePreferenceEntity(user, genreNameRaw, scoreChange));
    }

    private void createArtistPreference(UserEntity user, String sourceArtistId, int scoreChange) {
        if (scoreChange <= 0) {
            return;
        }

        userArtistPreferenceLowService.save(new UserArtistPreferenceEntity(user, sourceArtistId, scoreChange));
    }
}
