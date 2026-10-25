package apptive.team5.alarmv2.dto;

public record AlarmDispatchV2Summary(
        long draftCount,
        long scheduledCount,
        long completedCount
) {
}
