package apptive.team5.alarmv2.event;

public record AlarmDispatchV2CreatedEvent(Long recipientId, String title, String content, String deepLink) {
}
