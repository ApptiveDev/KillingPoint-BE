package apptive.team5.admin.service;

import apptive.team5.admin.dto.AdminUgcItem;
import apptive.team5.admin.dto.AdminUgcSearchType;
import apptive.team5.admin.repository.AdminUgcQueryRepository;
import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryMemoEntity;
import apptive.team5.diary.service.DiaryLowService;
import apptive.team5.diary.service.DiaryLikeLowService;
import apptive.team5.diary.service.DiaryMemoLowService;
import apptive.team5.diary.service.DiaryOrderLowService;
import apptive.team5.diary.service.DiaryReportLowService;
import apptive.team5.diary.service.DiaryStoreLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUgcService {

    private final AdminUgcQueryRepository adminUgcQueryRepository;
    private final DiaryLowService diaryLowService;
    private final DiaryLikeLowService diaryLikeLowService;
    private final DiaryMemoLowService diaryMemoLowService;
    private final DiaryOrderLowService diaryOrderLowService;
    private final DiaryReportLowService diaryReportLowService;
    private final DiaryStoreLowService diaryStoreLowService;

    @Transactional(readOnly = true)
    public Page<AdminUgcItem> getPublicUgcItems(String filter, AdminUgcSearchType searchType, String query, Pageable pageable) {
        Page<DiaryEntity> diaryPage = adminUgcQueryRepository.findPublicUgcItems(
                isReportedFilter(filter),
                searchType,
                normalizedQuery(query),
                pageable
        );
        List<DiaryEntity> diaries = diaryPage.getContent();
        List<Long> diaryIds = diaries.stream()
                .map(DiaryEntity::getId)
                .toList();
        Map<Long, Long> reportCounts = diaryReportLowService.countByDiaryIds(diaryIds);
        LocalDateTime now = LocalDateTime.now();

        return diaryPage.map(diary -> AdminUgcItem.from(diary, reportCounts.getOrDefault(diary.getId(), 0L), now));
    }

    @Transactional
    public void saveMemo(Long diaryId, String content) {
        if (content == null || content.isBlank()) {
            return;
        }

        DiaryEntity diary = diaryLowService.findDiaryById(diaryId);
        diaryMemoLowService.save(new DiaryMemoEntity(content.trim(), diary));
    }

    @Transactional
    public void deleteDiary(Long diaryId) {
        DiaryEntity diary = diaryLowService.findByIdWithUser(diaryId);
        Long userId = diary.getUser().getId();

        diaryReportLowService.deleteByDiaryId(diaryId);
        diaryOrderLowService.deleteDiaryId(userId, diaryId);
        diaryLikeLowService.deleteByDiaryId(diaryId);
        diaryStoreLowService.deleteByDiaryId(diaryId);
        diaryMemoLowService.deleteByDiaryId(diaryId);
        diaryLowService.deleteDiary(diary);
    }

    private boolean isReportedFilter(String filter) {
        return "reported".equals(filter);
    }

    private String normalizedQuery(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }
        return query.trim().toLowerCase();
    }
}
