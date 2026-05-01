package apptive.team5.alarm.event;

public record AlarmCreatedEvent(
        Long receiverId,
        String title,
        String content,
        String deepLink
) {
}
