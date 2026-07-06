package apptive.team5.recommendation.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryScope;
import apptive.team5.diary.service.DiaryLikeLowService;
import apptive.team5.diary.service.DiaryLowService;
import apptive.team5.diary.service.DiaryStoreLowService;
import apptive.team5.recommendation.domain.MusicMetadataEntity;
import apptive.team5.recommendation.domain.MusicMetadataSourceType;
import apptive.team5.recommendation.domain.UserArtistPreferenceEntity;
import apptive.team5.recommendation.domain.UserGenrePreferenceEntity;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @InjectMocks
    private RecommendationService recommendationService;

    @Mock
    private DiaryLowService diaryLowService;
    @Mock
    private DiaryLikeLowService diaryLikeLowService;
    @Mock
    private DiaryStoreLowService diaryStoreLowService;
    @Mock
    private UserGenrePreferenceLowService userGenrePreferenceLowService;
    @Mock
    private UserArtistPreferenceLowService userArtistPreferenceLowService;
    @Mock
    private ExploreExposureService exploreExposureService;

    @Test
    @DisplayName("returns personalized results first")
    void getExploreRecommendations_returnsPersonalizedResultsFirst() {
        Long userId = 1L;
        DiaryEntity matched = createDiary(101L, "K-Pop", "artist-1", 1);
        DiaryEntity unmatched = createDiary(102L, "Rock", "artist-2", 2);
        DiaryEntity likedMatched = createDiary(103L, "K-Pop", "artist-1", 1);

        given(diaryLowService.findRecentExploreCandidates(any(), any(), any(), any(Pageable.class)))
                .willReturn(List.of(matched, unmatched, likedMatched));
        given(userGenrePreferenceLowService.findTop3ByUserId(userId))
                .willReturn(List.of(new UserGenrePreferenceEntity(TestUtil.makeUserEntityWithId(), "K-Pop", 9)));
        given(userArtistPreferenceLowService.findTop5ByUserId(userId))
                .willReturn(List.of(new UserArtistPreferenceEntity(TestUtil.makeUserEntityWithId(), "artist-1", 4)));
        given(diaryLikeLowService.findLikedDiaryIdsByUser(userId, List.of(101L, 102L, 103L)))
                .willReturn(Set.of(103L));
        given(diaryStoreLowService.findStoredDiaryIdsByUser(userId, List.of(101L, 102L, 103L)))
                .willReturn(Set.of());
        given(diaryLikeLowService.findLikeCountsByDiaryIds(List.of(101L, 102L, 103L)))
                .willReturn(java.util.Map.of(101L, 5L, 102L, 1L, 103L, 10L));
        given(exploreExposureService.findRecentlyExposedDiaryIds(userId)).willReturn(Set.of());

        List<RecommendationService.RecommendedDiaryResult> result =
                recommendationService.getExploreRecommendations(userId, Set.of(userId));

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().diary().getId()).isEqualTo(101L);
        assertThat(result.getFirst().recommended()).isTrue();
        assertThat(result.getFirst().reason()).isEqualTo("GENRE_AND_ARTIST_MATCH");
        assertThat(result).extracting(r -> r.diary().getId()).doesNotContain(103L);
    }

    @Test
    @DisplayName("returns cold start results")
    void getExploreRecommendations_returnsColdStartResults() {
        Long userId = 1L;
        DiaryEntity popular = createDiary(201L, null, null, 1);
        DiaryEntity lessPopular = createDiary(202L, null, null, 2);

        given(diaryLowService.findRecentExploreCandidates(any(), any(), any(), any(Pageable.class)))
                .willReturn(List.of(popular, lessPopular));
        given(userGenrePreferenceLowService.findTop3ByUserId(userId)).willReturn(List.of());
        given(userArtistPreferenceLowService.findTop5ByUserId(userId)).willReturn(List.of());
        given(diaryLikeLowService.findLikedDiaryIdsByUser(userId, List.of(201L, 202L))).willReturn(Set.of());
        given(diaryStoreLowService.findStoredDiaryIdsByUser(userId, List.of(201L, 202L))).willReturn(Set.of());
        given(diaryLikeLowService.findLikeCountsByDiaryIds(List.of(201L, 202L)))
                .willReturn(java.util.Map.of(201L, 10L, 202L, 1L));
        given(exploreExposureService.findRecentlyExposedDiaryIds(userId)).willReturn(Set.of());

        List<RecommendationService.RecommendedDiaryResult> result =
                recommendationService.getExploreRecommendations(userId, Set.of(userId));

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().diary().getId()).isEqualTo(201L);
        assertThat(result.getFirst().recommended()).isTrue();
        assertThat(result.getFirst().reason()).isEqualTo("COLD_START_POPULAR");
    }

    @Test
    @DisplayName("returns fallback random when there is no match")
    void getExploreRecommendations_returnsFallbackRandomWhenNoMatch() {
        Long userId = 1L;
        DiaryEntity first = createDiary(301L, "Rock", "artist-2", 1);
        DiaryEntity second = createDiary(302L, "Jazz", "artist-3", 2);

        given(diaryLowService.findRecentExploreCandidates(any(), any(), any(), any(Pageable.class)))
                .willReturn(List.of(first, second));
        given(userGenrePreferenceLowService.findTop3ByUserId(userId))
                .willReturn(List.of(new UserGenrePreferenceEntity(TestUtil.makeUserEntityWithId(), "K-Pop", 9)));
        given(userArtistPreferenceLowService.findTop5ByUserId(userId))
                .willReturn(List.of(new UserArtistPreferenceEntity(TestUtil.makeUserEntityWithId(), "artist-1", 4)));
        given(diaryLikeLowService.findLikedDiaryIdsByUser(userId, List.of(301L, 302L))).willReturn(Set.of());
        given(diaryStoreLowService.findStoredDiaryIdsByUser(userId, List.of(301L, 302L))).willReturn(Set.of());
        given(diaryLikeLowService.findLikeCountsByDiaryIds(List.of(301L, 302L)))
                .willReturn(java.util.Map.of(301L, 2L, 302L, 3L));
        given(exploreExposureService.findRecentlyExposedDiaryIds(userId)).willReturn(Set.of());

        List<RecommendationService.RecommendedDiaryResult> result =
                recommendationService.getExploreRecommendations(userId, Set.of(userId));

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(recommendedDiaryResult -> !recommendedDiaryResult.recommended());
        assertThat(result).allMatch(recommendedDiaryResult -> "FALLBACK_RANDOM".equals(recommendedDiaryResult.reason()));
    }

    @Test
    @DisplayName("excludes recent exposures first")
    void getExploreRecommendations_excludesRecentlyExposedDiariesFirst() {
        Long userId = 1L;
        DiaryEntity recentExposed = createDiary(401L, "K-Pop", "artist-1", 1);
        DiaryEntity nextCandidate = createDiary(402L, "K-Pop", "artist-1", 2);
        DiaryEntity fallbackCandidate = createDiary(403L, "Rock", "artist-2", 3);

        given(diaryLowService.findRecentExploreCandidates(any(), any(), any(), any(Pageable.class)))
                .willReturn(List.of(recentExposed, nextCandidate, fallbackCandidate));
        given(userGenrePreferenceLowService.findTop3ByUserId(userId))
                .willReturn(List.of(new UserGenrePreferenceEntity(TestUtil.makeUserEntityWithId(), "K-Pop", 9)));
        given(userArtistPreferenceLowService.findTop5ByUserId(userId))
                .willReturn(List.of(new UserArtistPreferenceEntity(TestUtil.makeUserEntityWithId(), "artist-1", 4)));
        given(diaryLikeLowService.findLikedDiaryIdsByUser(userId, List.of(401L, 402L, 403L))).willReturn(Set.of());
        given(diaryStoreLowService.findStoredDiaryIdsByUser(userId, List.of(401L, 402L, 403L))).willReturn(Set.of());
        given(diaryLikeLowService.findLikeCountsByDiaryIds(List.of(401L, 402L, 403L)))
                .willReturn(java.util.Map.of(401L, 5L, 402L, 4L, 403L, 1L));
        given(exploreExposureService.findRecentlyExposedDiaryIds(userId)).willReturn(Set.of(401L));

        List<RecommendationService.RecommendedDiaryResult> result =
                recommendationService.getExploreRecommendations(userId, Set.of(userId));

        assertThat(result.getFirst().diary().getId()).isEqualTo(402L);
        assertThat(result).extracting(r -> r.diary().getId()).contains(403L);
    }

    @Test
    @DisplayName("relaxes recent exposure filtering when candidates are short")
    void getExploreRecommendations_relaxesRecentExposureForFallback() {
        Long userId = 1L;
        DiaryEntity first = createDiary(501L, "Rock", "artist-2", 1);
        DiaryEntity second = createDiary(502L, "Jazz", "artist-3", 2);

        given(diaryLowService.findRecentExploreCandidates(any(), any(), any(), any(Pageable.class)))
                .willReturn(List.of(first, second));
        given(userGenrePreferenceLowService.findTop3ByUserId(userId))
                .willReturn(List.of(new UserGenrePreferenceEntity(TestUtil.makeUserEntityWithId(), "K-Pop", 9)));
        given(userArtistPreferenceLowService.findTop5ByUserId(userId))
                .willReturn(List.of(new UserArtistPreferenceEntity(TestUtil.makeUserEntityWithId(), "artist-1", 4)));
        given(diaryLikeLowService.findLikedDiaryIdsByUser(userId, List.of(501L, 502L))).willReturn(Set.of());
        given(diaryStoreLowService.findStoredDiaryIdsByUser(userId, List.of(501L, 502L))).willReturn(Set.of());
        given(diaryLikeLowService.findLikeCountsByDiaryIds(List.of(501L, 502L)))
                .willReturn(java.util.Map.of(501L, 2L, 502L, 3L));
        given(exploreExposureService.findRecentlyExposedDiaryIds(userId)).willReturn(Set.of(501L, 502L));

        List<RecommendationService.RecommendedDiaryResult> result =
                recommendationService.getExploreRecommendations(userId, Set.of(userId));

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(recommendedDiaryResult -> "FALLBACK_RANDOM".equals(recommendedDiaryResult.reason()));
        assertThat(result).extracting(r -> r.diary().getId()).containsExactlyInAnyOrder(501L, 502L);
    }

    private DiaryEntity createDiary(Long diaryId, String genre, String artistId, int daysAgo) {
        UserEntity owner = TestUtil.makeUserEntityWithId();
        ReflectionTestUtils.setField(owner, "id", diaryId + 1000);

        DiaryEntity diary = TestUtil.makeDiaryEntityWithScope(owner, DiaryScope.PUBLIC);
        ReflectionTestUtils.setField(diary, "id", diaryId);
        ReflectionTestUtils.setField(diary, "createDateTime", LocalDateTime.now().minusDays(daysAgo));
        ReflectionTestUtils.setField(diary, "updateDateTime", LocalDateTime.now().minusDays(daysAgo));

        if (genre != null || artistId != null) {
            diary.assignMusicMetadata(new MusicMetadataEntity(
                    MusicMetadataSourceType.ITUNES,
                    "track-" + diaryId,
                    artistId,
                    genre
            ));
        }

        return diary;
    }
}
