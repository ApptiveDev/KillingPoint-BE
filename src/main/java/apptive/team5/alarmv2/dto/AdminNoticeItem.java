package apptive.team5.alarmv2.dto;

import apptive.team5.alarmv2.domain.AdminNotice;

import java.time.format.DateTimeFormatter;

public record AdminNoticeItem(Long noticeId, String title, String displayTime) {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    public static AdminNoticeItem from(AdminNotice notice) {
        String displayTime = notice.getCreateDateTime() == null ? "—" : DATE_FORMAT.format(notice.getCreateDateTime());
        return new AdminNoticeItem(notice.getId(), notice.getTitle(), displayTime);
    }
}
