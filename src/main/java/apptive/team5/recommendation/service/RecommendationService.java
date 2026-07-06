package apptive.team5.recommendation.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryScope;
import apptive.team5.diary.service.DiaryLikeLowService;
import apptive.team5.diary.service.DiaryLowService;
import apptive.team5.diary.service.DiaryStoreLowService;
import apptive.team5.recommendation.domain.UserArtistPreferenceEntity;
import apptive.team5.recommendation.domain.UserGenrePreferenceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private static final int DEFAULT_LIMIT = 5;
    private static final int CANDIDATE_LIMIT = 300;
    private static final int RECENT_DAYS = 30;
    private static final List<DiaryScope> EXPLORE_SCOPES = List.of(DiaryScope.PUBLIC, DiaryScope.KILLING_PART);

    private final DiaryLowService diaryLowService;
    private final DiaryLikeLowService diaryLikeLowService;
    private final DiaryStoreLowService diaryStoreLowService;
    private final UserGenrePreferenceLowService userGenrePreferenceLowService;
    private final UserArtistPreferenceLowService userArtistPreferenceLowService;
    private final ExploreExposureService exploreExposureService;

    public List<RecommendedDiaryResult> getExploreRecommendations(Long userId, Set<Long> excludedUserIds) {
        List<DiaryEntity> recentCandidates = diaryLowService.findRecentExploreCandidates(
                excludedUserIds,
                EXPLORE_SCOPES,
                LocalDateTime.now().minusDays(RECENT_DAYS),
                PageRequest.of(0, CANDIDATE_LIMIT)
        );

        List<UserGenrePreferenceEntity> topGenres = userGenrePreferenceLowService.findTop3ByUserId(userId);
        List<UserArtistPreferenceEntity> topArtists = userArtistPreferenceLowService.findTop5ByUserId(userId);

        List<Long> candidateIds = recentCandidates.stream().map(DiaryEntity::getId).toList();
        Set<Long> likedDiaryIds = diaryLikeLowService.findLikedDiaryIdsByUser(userId, candidateIds);
        Set<Long> storedDiaryIds = diaryStoreLowService.findStoredDiaryIdsByUser(userId, candidateIds);
        Map<Long, Long> likeCounts = diaryLikeLowService.findLikeCountsByDiaryIds(candidateIds);
        Set<Long> recentlyExposedDiaryIds = exploreExposureService.findRecentlyExposedDiaryIds(userId);

        Map<String, Integer> genreScores = topGenres.stream()
                .collect(Collectors.toMap(UserGenrePreferenceEntity::getGenreNameRaw, UserGenrePreferenceEntity::getScore));
        Map<String, Integer> artistScores = topArtists.stream()
                .collect(Collectors.toMap(UserArtistPreferenceEntity::getSourceArtistId, UserArtistPreferenceEntity::getScore));

        List<RecommendedDiaryResult> selected = new ArrayList<>();
        Set<Long> selectedDiaryIds = new LinkedHashSet<>();

        if (!genreScores.isEmpty() || !artistScores.isEmpty()) {
            List<RecommendedDiaryResult> personalized = recentCandidates.stream()
                    .filter(diary -> diary.getMusicMetadata() != null)
                    .filter(diary -> !likedDiaryIds.contains(diary.getId()))
                    .filter(diary -> !storedDiaryIds.contains(diary.getId()))
                    .filter(diary -> !recentlyExposedDiaryIds.contains(diary.getId()))
                    .map(diary -> toPersonalizedResult(diary, likeCounts.getOrDefault(diary.getId(), 0L), genreScores, artistScores))
                    .filter(Objects::nonNull)
                    .sorted(RECOMMENDATION_ORDER)
                    .toList();

            addUntilLimit(selected, selectedDiaryIds, personalized);
        }

        List<RecommendedDiaryResult> fallback = (genreScores.isEmpty() && artistScores.isEmpty())
                ? buildColdStartResults(recentCandidates, likedDiaryIds, storedDiaryIds, likeCounts, selectedDiaryIds, recentlyExposedDiaryIds)
                : buildRandomFallbackResults(recentCandidates, likedDiaryIds, storedDiaryIds, likeCounts, selectedDiaryIds, recentlyExposedDiaryIds, true, true);

        addUntilLimit(selected, selectedDiaryIds, fallback);

        if (selected.size() < DEFAULT_LIMIT) {
            List<RecommendedDiaryResult> relaxedFallback = buildRandomFallbackResults(
                    recentCandidates,
                    likedDiaryIds,
                    storedDiaryIds,
                    likeCounts,
                    selectedDiaryIds,
                    recentlyExposedDiaryIds,
                    false,
                    true
            );
            addUntilLimit(selected, selectedDiaryIds, relaxedFallback);
        }

        if (selected.size() < DEFAULT_LIMIT) {
            List<DiaryEntity> allCandidates = diaryLowService.findExploreCandidates(
                    excludedUserIds,
                    EXPLORE_SCOPES,
                    PageRequest.of(0, CANDIDATE_LIMIT)
            );
            List<Long> allCandidateIds = allCandidates.stream().map(DiaryEntity::getId).toList();
            Set<Long> allLikedDiaryIds = diaryLikeLowService.findLikedDiaryIdsByUser(userId, allCandidateIds);
            Set<Long> allStoredDiaryIds = diaryStoreLowService.findStoredDiaryIdsByUser(userId, allCandidateIds);
            Map<Long, Long> allLikeCounts = diaryLikeLowService.findLikeCountsByDiaryIds(allCandidateIds);

            List<RecommendedDiaryResult> olderFallback = buildRandomFallbackResults(
                    allCandidates,
                    allLikedDiaryIds,
                    allStoredDiaryIds,
                    allLikeCounts,
                    selectedDiaryIds,
                    recentlyExposedDiaryIds,
                    false,
                    true
            );
            addUntilLimit(selected, selectedDiaryIds, olderFallback);

            if (selected.size() < DEFAULT_LIMIT) {
                List<RecommendedDiaryResult> relaxedInteractionFallback = buildRandomFallbackResults(
                        allCandidates,
                        allLikedDiaryIds,
                        allStoredDiaryIds,
                        allLikeCounts,
                        selectedDiaryIds,
                        recentlyExposedDiaryIds,
                        false,
                        false
                );
                addUntilLimit(selected, selectedDiaryIds, relaxedInteractionFallback);
            }
        }

        return selected;
    }

    private RecommendedDiaryResult toPersonalizedResult(
            DiaryEntity diary,
            Long likeCount,
            Map<String, Integer> genreScores,
            Map<String, Integer> artistScores
    ) {
        String genreName = diary.getMusicMetadata().getPrimaryGenreNameRaw();
        String artistId = diary.getMusicMetadata().getSourceArtistId();

        Integer genreScore = genreName == null ? null : genreScores.get(genreName);
        Integer artistScore = artistId == null ? null : artistScores.get(artistId);

        if (genreScore == null && artistScore == null) {
            return null;
        }

        double score = scoreGenre(genreScore) + scoreArtist(artistScore) + scorePopularity(likeCount) + scoreFreshness(diary.getCreateDateTime());
        return new RecommendedDiaryResult(diary, true, determineReason(genreScore, artistScore), score, likeCount);
    }

    private List<RecommendedDiaryResult> buildColdStartResults(
            List<DiaryEntity> candidates,
            Set<Long> likedDiaryIds,
            Set<Long> storedDiaryIds,
            Map<Long, Long> likeCounts,
            Set<Long> selectedDiaryIds,
            Set<Long> recentlyExposedDiaryIds
    ) {
        return candidates.stream()
                .filter(diary -> !selectedDiaryIds.contains(diary.getId()))
                .filter(diary -> !likedDiaryIds.contains(diary.getId()))
                .filter(diary -> !storedDiaryIds.contains(diary.getId()))
                .filter(diary -> !recentlyExposedDiaryIds.contains(diary.getId()))
                .map(diary -> new RecommendedDiaryResult(
                        diary,
                        true,
                        "COLD_START_POPULAR",
                        scorePopularity(likeCounts.getOrDefault(diary.getId(), 0L)) + scoreFreshness(diary.getCreateDateTime()),
                        likeCounts.getOrDefault(diary.getId(), 0L)
                ))
                .sorted(RECOMMENDATION_ORDER)
                .limit(DEFAULT_LIMIT)
                .toList();
    }

    private List<RecommendedDiaryResult> buildRandomFallbackResults(
            List<DiaryEntity> candidates,
            Set<Long> likedDiaryIds,
            Set<Long> storedDiaryIds,
            Map<Long, Long> likeCounts,
            Set<Long> selectedDiaryIds,
            Set<Long> recentlyExposedDiaryIds,
            boolean excludeRecentExposure,
            boolean excludeInteractions
    ) {
        List<DiaryEntity> fallbackPool = candidates.stream()
                .filter(diary -> !selectedDiaryIds.contains(diary.getId()))
                .filter(diary -> !excludeInteractions || !likedDiaryIds.contains(diary.getId()))
                .filter(diary -> !excludeInteractions || !storedDiaryIds.contains(diary.getId()))
                .filter(diary -> !excludeRecentExposure || !recentlyExposedDiaryIds.contains(diary.getId()))
                .collect(Collectors.toCollection(ArrayList::new));

        Collections.shuffle(fallbackPool);

        return fallbackPool.stream()
                .limit(DEFAULT_LIMIT)
                .map(diary -> new RecommendedDiaryResult(
                        diary,
                        false,
                        "FALLBACK_RANDOM",
                        0.0,
                        likeCounts.getOrDefault(diary.getId(), 0L)
                ))
                .toList();
    }

    private void addUntilLimit(
            List<RecommendedDiaryResult> selected,
            Set<Long> selectedDiaryIds,
            List<RecommendedDiaryResult> candidates
    ) {
        for (RecommendedDiaryResult result : candidates) {
            if (selected.size() >= DEFAULT_LIMIT) {
                break;
            }
            if (selectedDiaryIds.add(result.diary().getId())) {
                selected.add(result);
            }
        }
    }

    private double scoreGenre(Integer genreScore) {
        if (genreScore == null) {
            return 0.0;
        }
        return Math.min(10.0, Math.sqrt(genreScore) * 2.0);
    }

    private double scoreArtist(Integer artistScore) {
        if (artistScore == null) {
            return 0.0;
        }
        return Math.min(8.0, Math.sqrt(artistScore) * 1.5);
    }

    private double scorePopularity(Long likeCount) {
        return Math.min(5.0, Math.log1p(likeCount) * 1.2);
    }

    private double scoreFreshness(LocalDateTime createdAt) {
        long days = ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());

        if (days <= 1) return 4.0;
        if (days <= 3) return 3.0;
        if (days <= 7) return 2.0;
        if (days <= 14) return 1.0;
        if (days <= 30) return 0.5;
        return 0.0;
    }

    private String determineReason(Integer genreScore, Integer artistScore) {
        if (genreScore != null && artistScore != null) return "GENRE_AND_ARTIST_MATCH";
        if (genreScore != null) return "GENRE_MATCH";
        return "ARTIST_MATCH";
    }

    private static final Comparator<RecommendedDiaryResult> RECOMMENDATION_ORDER =
            Comparator.comparingDouble(RecommendedDiaryResult::score)
                    .reversed()
                    .thenComparing(RecommendedDiaryResult::likeCount, Comparator.reverseOrder())
                    .thenComparing(result -> result.diary().getCreateDateTime(), Comparator.reverseOrder())
                    .thenComparing(result -> result.diary().getId(), Comparator.reverseOrder());

    public record RecommendedDiaryResult(
            DiaryEntity diary,
            boolean recommended,
            String reason,
            double score,
            Long likeCount
    ) {}
}
