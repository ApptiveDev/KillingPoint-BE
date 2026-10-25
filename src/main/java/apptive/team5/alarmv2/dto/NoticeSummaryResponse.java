package apptive.team5.alarmv2.dto;

import apptive.team5.alarmv2.domain.AdminNotice;

import java.time.LocalDateTime;

public record NoticeSummaryResponse(Long noticeId, String title, LocalDateTime createdAt) {

    public NoticeSummaryResponse(AdminNotice notice) {
        this(notice.getId(), notice.getTitle(), notice.getCreateDateTime());
    }
}
