package apptive.team5.admin.service;

import apptive.team5.admin.dto.UserListItem;
import apptive.team5.admin.dto.UserDiaryItem;
import apptive.team5.admin.dto.UserSearchType;
import apptive.team5.admin.repository.UserManagementQueryRepository;
import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.service.DiaryReportLowService;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserManagementQueryRepository userManagementQueryRepository;
    private final DiaryReportLowService diaryReportLowService;
    private final UserLowService userLowService;

    @Transactional(readOnly = true)
    public Page<UserListItem> getUsers(UserSearchType searchType, String query, boolean lockedOnly, Pageable pageable) {
        return userManagementQueryRepository.findUsers(searchType, normalizedQuery(query), lockedOnly, pageable)
                .map(UserListItem::from);
    }

    @Transactional(readOnly = true)
    public UserListItem getUser(Long userId) {
        return UserListItem.from(userLowService.findById(userId));
    }

    @Transactional(readOnly = true)
    public Page<UserDiaryItem> getUserDiaries(Long userId, boolean reportedOnly, Pageable pageable) {
        Page<DiaryEntity> diaryPage = userManagementQueryRepository.findUserDiaries(userId, reportedOnly, pageable);
        List<Long> diaryIds = diaryPage.getContent()
                .stream()
                .map(DiaryEntity::getId)
                .toList();
        Map<Long, Long> reportCounts = diaryReportLowService.countByDiaryIds(diaryIds);

        return diaryPage.map(diary -> UserDiaryItem.from(diary, reportCounts.getOrDefault(diary.getId(), 0L)));
    }

    @Transactional
    public void changeUserLocked(Long userId, boolean locked) {
        UserEntity user = userLowService.findById(userId);
        user.changeLocked(locked);
    }

    private String normalizedQuery(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }
        return query.trim().toLowerCase();
    }
}
