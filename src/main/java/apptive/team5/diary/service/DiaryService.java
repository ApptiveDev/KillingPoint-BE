package apptive.team5.diary.service;

import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryOrderEntity;
import apptive.team5.diary.domain.DiaryScope;
import apptive.team5.diary.domain.DiaryStoreEntity;
import apptive.team5.diary.dto.*;
import apptive.team5.diary.mapper.DiaryResponseMapper;
import apptive.team5.subscribe.service.SubscribeLowService;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class DiaryService {

    private final UserLowService userLowService;
    private final DiaryLowService diaryLowService;
    private final DiaryOrderLowService diaryOrderLowService;
    private final DiaryLikeLowService diaryLikeLowService;
    private final DiaryResponseMapper diaryResponseMapper;
    private final SubscribeLowService subscribeLowService;
    private final DiaryReportLowService diaryReportLowService;
    private final DiaryStoreLowService diaryStoreLowService;

    @Transactional(readOnly = true)
    public Page<MyDiaryResponseDto> getMyDiaries(Long userId, Pageable pageable) {
        UserEntity foundUser = userLowService.getReferenceById(userId);

        Optional<DiaryOrderEntity> optionalDiaryOrder = diaryOrderLowService.findByUserId(userId);

        Page<DiaryEntity> diaryPage = getSortedDiaries(pageable, optionalDiaryOrder, foundUser);

        return getDiaryResponseDtoPage(userId, diaryPage, MyDiaryResponseDto::from);
    }

    @Transactional(readOnly = true)
    public Page<FeedDiaryResponseDto> getDiariesFeeds(Long userId, Pageable pageable) {

        // 구독 중인 아이디
        Set<Long> subscribedToIds = subscribeLowService.findBySubscriberId(userId)
                .stream().map(subscribe -> subscribe.getSubscribedTo().getId()).collect(Collectors.toSet());

        List<DiaryScope> visibleScopes = List.of(DiaryScope.PUBLIC, DiaryScope.KILLING_PART);
        Page<DiaryEntity> diaryPage = diaryLowService.findByUserIdsAndScopseWithUserPage(subscribedToIds, visibleScopes, pageable);

        return getDiaryResponseDtoPage(userId, diaryPage, FeedDiaryResponseDto::from);
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

        return getDiaryResponseDtoPage(currentUserId, diaryPage, UserDiaryResponseDto::from);
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

    @Transactional(readOnly = true)
    public List<FeedDiaryResponseDto> getRandomDiaries(Long userId) {
        List<DiaryEntity> randomDiary = diaryLowService.findRandomDiary();

        Collections.shuffle(randomDiary);

        return getDiaryResponseDtoList(userId, randomDiary, FeedDiaryResponseDto::from);
    }

    @Transactional(readOnly = true)
    public Page<FeedDiaryResponseDto> getStoredDiaries(Long userId, Pageable pageable) {

        Page<DiaryEntity> diaryPage = diaryStoreLowService.findStoredDiaryByUserWithDiary(userId, pageable)
                .map(DiaryStoreEntity::getDiary);

        return getDiaryResponseDtoPage(userId, diaryPage, FeedDiaryResponseDto::from);
    }

    public DiaryEntity createDiary(Long userId, DiaryCreateRequest diaryRequest) {
        UserEntity foundUser = userLowService.getReferenceById(userId);

        DiaryEntity diary = diaryRequest.toEntity(foundUser);

        DiaryEntity savedDiary = diaryLowService.saveDiary(diary);

        diaryOrderLowService.addDiaryId(userId, savedDiary.getId());

        return savedDiary;
    }

    public void updateDiary(Long userId, Long diaryId, DiaryUpdateRequestDto updateRequest) {
        UserEntity foundUser = userLowService.getReferenceById(userId);

        DiaryEntity foundDiary = diaryLowService.findDiaryById(diaryId);

        foundDiary.validateOwner(foundUser);

        diaryLowService.updateDiary(foundDiary, updateRequest.toDomainInfo());
    }

    public void deleteDiary(Long userId, Long diaryId) {
        UserEntity foundUser = userLowService.getReferenceById(userId);

        DiaryEntity foundDiary = diaryLowService.findDiaryById(diaryId);

        foundDiary.validateOwner(foundUser);

        diaryStoreLowService.deleteByDiaryId(diaryId);
        diaryReportLowService.deleteByDiaryId(diaryId);
        diaryOrderLowService.deleteDiaryId(userId, diaryId);
        diaryLikeLowService.deleteByDiaryId(diaryId);
        diaryLowService.deleteDiary(foundDiary);
    }

    public void deleteByUserId(Long userId) {

        List<Long> diaryIds = diaryLowService.findDiaryByUserId(userId)
                .stream()
                .map(DiaryEntity::getId)
                .toList();

        diaryStoreLowService.deleteByDiaryIds(diaryIds);

        diaryReportLowService.deleteByDiaryIds(diaryIds);

        diaryLikeLowService.deleteByDiaryIds(diaryIds);

        diaryLowService.deleteByUserId(userId);
    }

    private <T extends DiaryResponseDto> Page<T> getDiaryResponseDtoPage(Long userId, Page<DiaryEntity> diaryPage, DiaryResponseMapper.DiaryResponseDtoMapper<T> mapper) {
        List<Long> diaryIds = diaryPage.getContent().stream()
                .map(DiaryEntity::getId)
                .toList();

        Set<Long> likedDiaryIds = diaryLikeLowService.findLikedDiaryIdsByUser(userId, diaryIds);
        Map<Long, Long> likeCountsMap = diaryLikeLowService.findLikeCountsByDiaryIds(diaryIds);
        Set<Long> storedDiaryIds = diaryStoreLowService.findStoredDiaryIdsByUser(userId, diaryIds);

        return diaryResponseMapper.mapToResponseDto(
                diaryPage,
                likedDiaryIds,
                storedDiaryIds,
                likeCountsMap,
                userId,
                mapper
        );
    }

    private <T extends DiaryResponseDto> List<T> getDiaryResponseDtoList(Long userId, List<DiaryEntity> diaries, DiaryResponseMapper.DiaryResponseDtoMapper<T> mapper) {
        List<Long> diaryIds = diaries.stream()
                .map(DiaryEntity::getId)
                .toList();

        Set<Long> likedDiaryIds = diaryLikeLowService.findLikedDiaryIdsByUser(userId, diaryIds);
        Map<Long, Long> likeCountsMap = diaryLikeLowService.findLikeCountsByDiaryIds(diaryIds);
        Set<Long> storedDiaryIds = diaryStoreLowService.findStoredDiaryIdsByUser(userId, diaryIds);

        return diaryResponseMapper.mapToResponseDto(
                diaries,
                likedDiaryIds,
                storedDiaryIds,
                likeCountsMap,
                userId,
                mapper
        );
    }

    private Page<DiaryEntity> getSortedDiaries(Pageable pageable, Optional<DiaryOrderEntity> optionalDiaryOrder, UserEntity foundUser) {
        Page<DiaryEntity> diaryPage;

        if (optionalDiaryOrder.isPresent()) {
            List<Long> diaryOrder = optionalDiaryOrder.get().getOrderList();

            diaryPage = getOrderedDiariesPage(diaryOrder, pageable);
        }
        else {
            diaryPage = diaryLowService.findDiaryByUser(foundUser, pageable);
        }
        return diaryPage;
    }

    private Page<DiaryEntity> getOrderedDiariesPage(List<Long> diaryOrder, Pageable pageable) {
        int st = (int) pageable.getOffset();
        int en = Math.min(st + pageable.getPageSize(), diaryOrder.size());

        if (st > diaryOrder.size()) {
            return Page.empty();
        }

        List<Long> pagedIds = diaryOrder.subList(st, en);

        List<DiaryEntity> pagedDiaries = diaryLowService.findAllByIds(pagedIds);

        Map<Long, DiaryEntity> entityMap = mapDiariesById(pagedDiaries);

        List<DiaryEntity> orderedDiaries = pagedIds.stream()
                .map(entityMap::get)
                .filter(Objects::nonNull)
                .toList();

        return new PageImpl<>(orderedDiaries, pageable, diaryOrder.size());
    }

    private Map<Long, DiaryEntity> mapDiariesById(List<DiaryEntity> diaries) {
        return diaries.stream()
                .collect(Collectors.toMap(DiaryEntity::getId, Function.identity()));
    }
}
