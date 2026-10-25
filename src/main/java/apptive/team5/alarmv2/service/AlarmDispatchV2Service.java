package apptive.team5.alarmv2.service;

import apptive.team5.alarmv2.domain.AlarmDispatchV2Status;
import apptive.team5.alarmv2.domain.AlarmDispatchV2;
import apptive.team5.alarmv2.domain.AlarmDispatchV2Recipient;
import apptive.team5.alarmv2.domain.AlarmDispatchV2RecipientMode;
import apptive.team5.alarmv2.domain.AlarmDispatchV2Type;
import apptive.team5.alarmv2.dto.AlarmDispatchV2Summary;
import apptive.team5.alarmv2.dto.AlarmDispatchV2HistoryItem;
import apptive.team5.alarmv2.dto.AlarmDispatchV2ScheduleItem;
import apptive.team5.alarmv2.event.AlarmDispatchV2CreatedEvent;
import apptive.team5.alarmv2.repository.AlarmDispatchV2RecipientRepository;
import apptive.team5.alarmv2.repository.AlarmDispatchV2Repository;
import apptive.team5.alarmv2.repository.AlarmV2RecipientQueryRepository;
import apptive.team5.alarm.entity.Alarm;
import apptive.team5.alarm.entity.AlarmMessage;
import apptive.team5.alarm.service.AlarmLowService;
import apptive.team5.admin.dto.UserSearchType;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AlarmDispatchV2Service {

    private final AlarmDispatchV2Repository alarmDispatchV2Repository;
    private final AlarmDispatchV2RecipientRepository recipientRepository;
    private final AlarmV2RecipientQueryRepository recipientQueryRepository;
    private final UserRepository userRepository;
    private final AlarmLowService alarmLowService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public AlarmDispatchV2Summary getSummary() {
        return new AlarmDispatchV2Summary(
                alarmDispatchV2Repository.countByStatus(AlarmDispatchV2Status.DRAFT),
                alarmDispatchV2Repository.countByStatus(AlarmDispatchV2Status.SCHEDULED),
                alarmDispatchV2Repository.countByStatus(AlarmDispatchV2Status.COMPLETED)
        );
    }

    @Transactional(readOnly = true)
    public List<AlarmDispatchV2HistoryItem> getRecentDispatches() {
        return alarmDispatchV2Repository.findTop20ByOrderByCreateDateTimeDesc(PageRequest.of(0, 20))
                .stream().map(AlarmDispatchV2HistoryItem::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AlarmDispatchV2ScheduleItem> getScheduledDispatches() {
        return alarmDispatchV2Repository.findByStatusOrderByScheduledAtAsc(AlarmDispatchV2Status.SCHEDULED)
                .stream().map(AlarmDispatchV2ScheduleItem::from).toList();
    }

    @Transactional
    public void cancelScheduledDispatch(Long dispatchId) {
        AlarmDispatchV2 dispatch = alarmDispatchV2Repository.findById(dispatchId)
                .orElseThrow(() -> new IllegalArgumentException("예약 알림을 찾을 수 없습니다."));
        if (dispatch.getStatus() != AlarmDispatchV2Status.SCHEDULED) {
            throw new IllegalArgumentException("대기 중인 예약만 취소할 수 있습니다.");
        }
        dispatch.changeStatus(AlarmDispatchV2Status.CANCELLED);
    }

    @Transactional
    public void processDueScheduledDispatches() {
        List<AlarmDispatchV2> dueDispatches = alarmDispatchV2Repository
                .findTop20ByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(
                        AlarmDispatchV2Status.SCHEDULED, LocalDateTime.now());
        for (AlarmDispatchV2 dispatch : dueDispatches) {
            try {
                dispatch.changeStatus(AlarmDispatchV2Status.PROCESSING);
                int pageNumber = 0;
                Page<Long> recipientPage;
                do {
                    recipientPage = recipientRepository.findRecipientUserIds(
                            dispatch.getId(), PageRequest.of(pageNumber++, 500));
                    deliverToUsers(recipientPage.getContent(), dispatch.getTitle(), dispatch.getContent(), dispatch.getDeepLink());
                } while (recipientPage.hasNext());
                dispatch.changeStatus(AlarmDispatchV2Status.COMPLETED);
            } catch (RuntimeException exception) {
                dispatch.changeStatus(AlarmDispatchV2Status.FAILED);
            }
        }
    }

    @Transactional
    public AlarmDispatchV2 createDispatch(String title,
                                          String content,
                                          String deepLink,
                                          String recipientModeValue,
                                          List<Long> requestedRecipientIds,
                                          String dispatchTypeValue,
                                          LocalDateTime scheduledAt) {
        String normalizedTitle = title == null ? "" : title.trim();
        String normalizedContent = content == null ? "" : content.trim();
        String normalizedDeepLink = deepLink == null || deepLink.isBlank() ? "/" : deepLink.trim();
        if (normalizedTitle.isBlank()) throw new IllegalArgumentException("제목을 입력해주세요.");
        if (normalizedTitle.length() > 200) throw new IllegalArgumentException("제목은 200자 이내로 입력해주세요.");
        if (normalizedContent.isBlank()) throw new IllegalArgumentException("본문을 입력해주세요.");
        if (normalizedContent.length() > 1000) throw new IllegalArgumentException("본문은 1000자 이내로 입력해주세요.");
        if (normalizedDeepLink.length() > 1000) throw new IllegalArgumentException("딥링크는 1000자 이내로 입력해주세요.");

        AlarmDispatchV2RecipientMode recipientMode = "manual".equals(recipientModeValue)
                ? AlarmDispatchV2RecipientMode.SELECTED_USERS
                : AlarmDispatchV2RecipientMode.ALL_ELIGIBLE;
        AlarmDispatchV2Type dispatchType = "SCHEDULED".equals(dispatchTypeValue)
                ? AlarmDispatchV2Type.SCHEDULED
                : AlarmDispatchV2Type.IMMEDIATE;
        if (dispatchType == AlarmDispatchV2Type.SCHEDULED && (scheduledAt == null || !scheduledAt.isAfter(LocalDateTime.now()))) {
            throw new IllegalArgumentException("예약 시간은 현재보다 이후로 설정해주세요.");
        }

        List<Long> recipientIds = recipientMode == AlarmDispatchV2RecipientMode.ALL_ELIGIBLE
                ? null
                : findSelectableRequestedIds(requestedRecipientIds);
        int recipientCount = recipientMode == AlarmDispatchV2RecipientMode.ALL_ELIGIBLE
                ? Math.toIntExact(recipientQueryRepository.countSelectableRecipients(UserSearchType.USER_ID, ""))
                : recipientIds.size();
        if (recipientCount == 0) throw new IllegalArgumentException("발송 가능한 유저가 없습니다.");

        AlarmDispatchV2 dispatch = alarmDispatchV2Repository.save(new AlarmDispatchV2(
                normalizedTitle,
                normalizedContent,
                normalizedDeepLink,
                recipientMode,
                dispatchType,
                dispatchType == AlarmDispatchV2Type.SCHEDULED ? AlarmDispatchV2Status.SCHEDULED : AlarmDispatchV2Status.PROCESSING,
                recipientCount,
                dispatchType == AlarmDispatchV2Type.SCHEDULED ? scheduledAt : null
        ));

        if (dispatchType == AlarmDispatchV2Type.SCHEDULED) {
            if (recipientIds == null) recipientIds = findAllSelectableRecipientIds();
            saveRecipientSnapshot(dispatch, recipientIds);
            return dispatch;
        }

        if (recipientIds == null) {
            dispatchAllEligible(dispatch, normalizedTitle, normalizedContent, normalizedDeepLink);
        } else {
            dispatchToUsers(dispatch, recipientIds, normalizedTitle, normalizedContent, normalizedDeepLink);
        }
        dispatch.changeStatus(AlarmDispatchV2Status.COMPLETED);
        return dispatch;
    }

    private List<Long> findSelectableRequestedIds(List<Long> requestedIds) {
        if (requestedIds == null || requestedIds.isEmpty()) return List.of();
        Set<Long> requested = new LinkedHashSet<>(requestedIds);
        List<Long> eligible = userRepository.findAllById(requested).stream()
                .filter(user -> !user.isLocked() && user.isAlarmEnabled())
                .map(UserEntity::getId)
                .toList();
        if (eligible.size() != requested.size()) {
            throw new IllegalArgumentException("선택한 유저 중 발송할 수 없는 유저가 있습니다. 대상 목록을 새로고침해주세요.");
        }
        return eligible;
    }

    private List<Long> findAllSelectableRecipientIds() {
        List<Long> ids = new ArrayList<>();
        int pageNumber = 0;
        Page<Long> page;
        do {
            page = recipientQueryRepository.findSelectableRecipientIds(PageRequest.of(pageNumber++, 500));
            ids.addAll(page.getContent());
        } while (page.hasNext());
        return ids;
    }

    private void saveRecipientSnapshot(AlarmDispatchV2 dispatch, List<Long> recipientIds) {
        for (int from = 0; from < recipientIds.size(); from += 500) {
            int to = Math.min(from + 500, recipientIds.size());
            List<AlarmDispatchV2Recipient> recipients = recipientIds.subList(from, to).stream()
                    .map(id -> new AlarmDispatchV2Recipient(dispatch, id))
                    .toList();
            recipientRepository.saveAll(recipients);
        }
    }

    private void dispatchAllEligible(AlarmDispatchV2 dispatch, String title, String content, String deepLink) {
        int pageNumber = 0;
        Page<Long> page;
        do {
            page = recipientQueryRepository.findSelectableRecipientIds(PageRequest.of(pageNumber++, 500));
            List<Long> ids = page.getContent();
            dispatchToUsers(dispatch, ids, title, content, deepLink);
        } while (page.hasNext());
    }

    private void dispatchToUsers(AlarmDispatchV2 dispatch, List<Long> recipientIds, String title, String content, String deepLink) {
        if (recipientIds.isEmpty()) return;
        saveRecipientSnapshot(dispatch, recipientIds);
        deliverToUsers(recipientIds, title, content, deepLink);
    }

    private void deliverToUsers(List<Long> recipientIds, String title, String content, String deepLink) {
        if (recipientIds.isEmpty()) return;
        List<UserEntity> users = userRepository.findAllById(recipientIds);
        users.forEach(user -> {
            alarmLowService.save(new Alarm(title, content, deepLink, user, AlarmMessage.ADMIN_ALARM));
            eventPublisher.publishEvent(new AlarmDispatchV2CreatedEvent(user.getId(), title, content, deepLink));
        });
    }
}
