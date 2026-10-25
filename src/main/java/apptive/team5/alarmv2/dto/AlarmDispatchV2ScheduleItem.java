package apptive.team5.alarmv2.dto;

import apptive.team5.alarmv2.domain.AlarmDispatchV2;

import java.time.format.DateTimeFormatter;

public record AlarmDispatchV2ScheduleItem(Long id, String title, int recipientCount, String scheduledAt) {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    public static AlarmDispatchV2ScheduleItem from(AlarmDispatchV2 dispatch) {
        String scheduledAt = dispatch.getScheduledAt() == null ? "—" : DATE_FORMAT.format(dispatch.getScheduledAt());
        return new AlarmDispatchV2ScheduleItem(dispatch.getId(), dispatch.getTitle(), dispatch.getRecipientCount(), scheduledAt);
    }
}
