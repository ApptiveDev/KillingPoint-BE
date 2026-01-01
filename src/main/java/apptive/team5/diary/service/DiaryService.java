package apptive.team5.diary.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryScope;
import apptive.team5.diary.dto.*;
import apptive.team5.diary.mapper.DiaryResponseMapper;
import apptive.team5.subscribe.service.SubscribeLowService;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final UserLowService userLowService;
    private final DiaryLowService diaryLowService;
    private final DiaryLikeLowService diaryLikeLowService;
    private final DiaryResponseMapper diaryResponseMapper;
    private final SubscribeLowService subscribeLowService;

    @Transactional(readOnly = true)
    public Page<MyDiaryResponseDto> getMyDiaries(Long userId, Pageable pageable) {
        UserEntity foundUser = userLowService.getReferenceById(userId);

        Page<DiaryEntity> diaryPage = diaryLowService.findDiaryByUser(foundUser, pageable);

        List<Long> diaryIds = diaryPage.stream()
                .map(DiaryEntity::getId)
                .toList();


        Set<Long> likedDiaryIds = diaryLikeLowService.findLikedDiaryIdsByUser(userId, diaryIds);
        Map<Long, Long> likeCountsMaps = diaryLikeLowService.findLikeCountsByDiaryIds(diaryIds);

        return diaryResponseMapper.mapToResponseDto(
                diaryPage,
                likedDiaryIds,
                likeCountsMaps,
                userId,
                MyDiaryResponseDto::from
        );
    }

    @Transactional(readOnly = true)
    public Page<FeedDiaryResponseDto> getDiariesFeeds(Long userId, Pageable pageable) {

        // 구독 중인 아이디
        Set<Long> subscribedToIds = subscribeLowService.findBySubscriberId(userId)
                .stream().map(subscribe -> subscribe.getSubscribedTo().getId()).collect(Collectors.toSet());

        List<DiaryScope> visibleScopes = List.of(DiaryScope.PUBLIC, DiaryScope.KILLING_PART);
        Page<DiaryEntity> diaryPage = diaryLowService.findByUserIdsAndScopseWithUserPage(subscribedToIds, visibleScopes, pageable);

        List<Long> diaryIds = diaryPage.getContent().stream()
                .map(DiaryEntity::getId)
                .toList();

        Set<Long> likedDiaryIds = diaryLikeLowService.findLikedDiaryIdsByUser(userId, diaryIds);
        Map<Long, Long> likeCountsMap = diaryLikeLowService.findLikeCountsByDiaryIds(diaryIds);

        return diaryResponseMapper.mapToResponseDto(
                diaryPage,
                likedDiaryIds,
                likeCountsMap,
                userId,
                FeedDiaryResponseDto::from
        );
    }


    @Transactional(readOnly = true)
    public Page<UserDiaryResponseDto> getUserDiaries(Long targetUserId, Long currentUserId, Pageable pageable) {
        Page<DiaryEntity> diaryPage;

        if (targetUserId.equals(currentUserId)) {
            UserEntity targetUser = userLowService.getReferenceById(targetUserId);
            diaryPage = diaryLowService.findDiaryByUser(targetUser, pageable);
        }
        else {
            List<DiaryScope> visibleScopes = List.of(DiaryScope.PUBLIC, DiaryScope.KILLING_PART);
            diaryPage = diaryLowService.findDiaryByUserAndScopeIn(targetUserId, visibleScopes, pageable);
        }

        List<Long> diaryIds = diaryPage.getContent().stream()
                .map(DiaryEntity::getId)
                .toList();

        Set<Long> likedDiaryIds = diaryLikeLowService.findLikedDiaryIdsByUser(currentUserId, diaryIds);
        Map<Long, Long> likeCountsMap = diaryLikeLowService.findLikeCountsByDiaryIds(diaryIds);

        return diaryResponseMapper.mapToResponseDto(
                diaryPage,
                likedDiaryIds,
                likeCountsMap,
                currentUserId,
                UserDiaryResponseDto::from
        );
    }

    @Transactional(readOnly = true)
    public List<CalendarDiaryResponseDto> getMyDiariesByPeriod(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return diaryLowService.findByUserIdAndPeriod(userId, startDateTime, endDateTime)
                .stream()
                .map(CalendarDiaryResponseDto::from)
                .toList();
    }

    @Transactional
    public DiaryEntity createDiary(Long userId, DiaryCreateRequest diaryRequest) {
        UserEntity foundUser = userLowService.getReferenceById(userId);

        DiaryEntity diary = diaryRequest.toEntity(foundUser);

        return diaryLowService.saveDiary(diary);
    }

    @Transactional
    public void updateDiary(Long userId, Long diaryId, DiaryUpdateRequestDto updateRequest) {
        UserEntity foundUser = userLowService.getReferenceById(userId);

        DiaryEntity foundDiary = diaryLowService.findDiaryById(diaryId);

        foundDiary.validateOwner(foundUser);

        diaryLowService.updateDiary(foundDiary, updateRequest.toDomainInfo());
    }

    @Transactional
    public void deleteDiary(Long userId, Long diaryId) {
        UserEntity foundUser = userLowService.getReferenceById(userId);

        DiaryEntity foundDiary = diaryLowService.findDiaryById(diaryId);

        foundDiary.validateOwner(foundUser);

        diaryLikeLowService.deleteByDiaryId(diaryId);
        diaryLowService.deleteDiary(foundDiary);
    }

    @Transactional
    public void deleteByUserId(Long userId) {

        List<Long> diaryIds = diaryLowService.findDiaryByUserId(userId)
                .stream()
                .map(DiaryEntity::getId)
                .toList();

        diaryLikeLowService.deleteByDiaryIds(diaryIds);

        diaryLowService.deleteByUserId(userId);
    }
}
