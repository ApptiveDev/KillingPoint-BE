package apptive.team5.alarmv2.service;

import apptive.team5.alarmv2.domain.AdminNotice;
import apptive.team5.alarmv2.dto.AdminNoticeItem;
import apptive.team5.alarmv2.dto.NoticeDetailResponse;
import apptive.team5.alarmv2.dto.NoticeSummaryResponse;
import apptive.team5.alarmv2.repository.AdminNoticeRepository;
import apptive.team5.alarmv2.event.AlarmDispatchV2CreatedEvent;
import apptive.team5.alarm.entity.Alarm;
import apptive.team5.alarm.entity.AlarmMessage;
import apptive.team5.alarm.service.AlarmLowService;
import apptive.team5.user.repository.UserRepository;
import apptive.team5.user.domain.UserEntity;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import apptive.team5.global.exception.NotFoundEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminNoticeService {

    private static final int MAX_CONTENT_LENGTH = 20_000;

    private final AdminNoticeRepository adminNoticeRepository;
    private final UserRepository userRepository;
    private final AlarmLowService alarmLowService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void publish(String title, String content) {
        String normalizedTitle = title == null ? "" : title.trim();
        String normalizedContent = content == null ? "" : content.trim();
        if (normalizedTitle.isBlank()) throw new IllegalArgumentException("공지 제목을 입력해주세요.");
        if (normalizedTitle.length() > 200) throw new IllegalArgumentException("공지 제목은 200자 이내로 입력해주세요.");
        if (normalizedContent.isBlank()) throw new IllegalArgumentException("공지 본문을 입력해주세요.");
        if (normalizedContent.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("공지 본문은 20,000자 이내로 입력해주세요.");
        }
        AdminNotice notice = adminNoticeRepository.save(new AdminNotice(normalizedTitle, normalizedContent));
        String alarmTitle = "새 공지가 등록됐어요";
        String alarmContent = normalizedTitle;
        String deepLink = "/api/notices/" + notice.getId();

        int pageNumber = 0;
        Page<UserEntity> users;
        do {
            users = userRepository.findAll(PageRequest.of(pageNumber++, 500, Sort.by(Sort.Direction.ASC, "id")));
            users.forEach(user -> {
                alarmLowService.save(new Alarm(alarmTitle, alarmContent, deepLink, user, AlarmMessage.ADMIN_ALARM));
                eventPublisher.publishEvent(new AlarmDispatchV2CreatedEvent(user.getId(), alarmTitle, alarmContent, deepLink));
            });
        } while (users.hasNext());
    }

    @Transactional(readOnly = true)
    public List<AdminNoticeItem> getRecentNotices() {
        return adminNoticeRepository.findTop20ByOrderByCreateDateTimeDesc(PageRequest.of(0, 20))
                .stream().map(AdminNoticeItem::from).toList();
    }

    @Transactional(readOnly = true)
    public Page<NoticeSummaryResponse> getNotices(Pageable pageable) {
        return adminNoticeRepository.findAll(pageable).map(NoticeSummaryResponse::new);
    }

    @Transactional(readOnly = true)
    public NoticeDetailResponse getNotice(Long noticeId) {
        AdminNotice notice = adminNoticeRepository.findById(noticeId)
                .orElseThrow(() -> new NotFoundEntityException("공지를 찾을 수 없습니다."));
        return new NoticeDetailResponse(notice);
    }
}
