package apptive.team5.alarm.dto;

public record SubscribeAlarmSendRequest(
        Long subscribedToUserId,
        Long subscriberId
) {
}
