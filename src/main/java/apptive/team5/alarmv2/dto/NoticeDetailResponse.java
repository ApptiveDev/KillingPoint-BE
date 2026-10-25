package apptive.team5.alarmv2.dto;

import apptive.team5.alarmv2.domain.AdminNotice;

import java.time.LocalDateTime;

public record NoticeDetailResponse(Long noticeId, String title, String content, LocalDateTime createdAt) {

    public NoticeDetailResponse(AdminNotice notice) {
        this(notice.getId(), notice.getTitle(), notice.getContent(), notice.getCreateDateTime());
    }
}
