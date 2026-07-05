package apptive.team5.recommendation.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.recommendation.domain.MusicMetadataEntity;
import apptive.team5.recommendation.domain.MusicMetadataSourceType;
import apptive.team5.recommendation.domain.UserArtistPreferenceEntity;
import apptive.team5.recommendation.domain.UserGenrePreferenceEntity;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PreferenceServiceTest {

    @InjectMocks
    private PreferenceService preferenceService;

    @Mock
    private UserGenrePreferenceLowService userGenrePreferenceLowService;
    @Mock
    private UserArtistPreferenceLowService userArtistPreferenceLowService;

    @Test
    @DisplayName("작성 반영 시 장르와 아티스트 선호도가 생성된다")
    void reflectDiaryCreated_createsGenreAndArtistPreferences() {
        UserEntity user = TestUtil.makeUserEntityWithId();
        DiaryEntity diary = createDiaryWithMetadata(user);

        given(userGenrePreferenceLowService.findByUserIdAndGenreNameRaw(user.getId(), "K-Pop"))
                .willReturn(Optional.empty());
        given(userArtistPreferenceLowService.findByUserIdAndSourceArtistId(user.getId(), "artist-1"))
                .willReturn(Optional.empty());

        preferenceService.reflectDiaryCreated(user, diary);

        ArgumentCaptor<UserGenrePreferenceEntity> genreCaptor = ArgumentCaptor.forClass(UserGenrePreferenceEntity.class);
        ArgumentCaptor<UserArtistPreferenceEntity> artistCaptor = ArgumentCaptor.forClass(UserArtistPreferenceEntity.class);

        verify(userGenrePreferenceLowService).save(genreCaptor.capture());
        verify(userArtistPreferenceLowService).save(artistCaptor.capture());

        assertThat(genreCaptor.getValue().getGenreNameRaw()).isEqualTo("K-Pop");
        assertThat(genreCaptor.getValue().getScore()).isEqualTo(5);
        assertThat(artistCaptor.getValue().getSourceArtistId()).isEqualTo("artist-1");
        assertThat(artistCaptor.getValue().getScore()).isEqualTo(5);
    }

    @Test
    @DisplayName("삭제 반영 시 점수가 0 이하가 되면 선호도가 삭제된다")
    void reflectDiaryDeleted_deletesPreferencesWhenScoreBecomesZero() {
        UserEntity user = TestUtil.makeUserEntityWithId();
        DiaryEntity diary = createDiaryWithMetadata(user);
        UserGenrePreferenceEntity genrePreference = new UserGenrePreferenceEntity(user, "K-Pop", 5);
        UserArtistPreferenceEntity artistPreference = new UserArtistPreferenceEntity(user, "artist-1", 5);

        given(userGenrePreferenceLowService.findByUserIdAndGenreNameRaw(user.getId(), "K-Pop"))
                .willReturn(Optional.of(genrePreference));
        given(userArtistPreferenceLowService.findByUserIdAndSourceArtistId(user.getId(), "artist-1"))
                .willReturn(Optional.of(artistPreference));

        preferenceService.reflectDiaryDeleted(user, diary);

        verify(userGenrePreferenceLowService).delete(genrePreference);
        verify(userArtistPreferenceLowService).delete(artistPreference);
    }

    @Test
    @DisplayName("메타데이터가 없으면 선호도는 반영되지 않는다")
    void reflectDiaryLiked_withoutMetadata_doesNothing() {
        UserEntity user = TestUtil.makeUserEntityWithId();
        DiaryEntity diary = TestUtil.makeDiaryEntity(user);

        preferenceService.reflectDiaryLiked(user, diary);

        verify(userGenrePreferenceLowService, never()).save(org.mockito.ArgumentMatchers.any());
        verify(userArtistPreferenceLowService, never()).save(org.mockito.ArgumentMatchers.any());
        verify(userGenrePreferenceLowService, never()).delete(org.mockito.ArgumentMatchers.any());
        verify(userArtistPreferenceLowService, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    private DiaryEntity createDiaryWithMetadata(UserEntity user) {
        DiaryEntity diary = TestUtil.makeDiaryEntity(user);
        diary.assignMusicMetadata(new MusicMetadataEntity(
                MusicMetadataSourceType.ITUNES,
                "track-1",
                "artist-1",
                "K-Pop"
        ));
        return diary;
    }
}
