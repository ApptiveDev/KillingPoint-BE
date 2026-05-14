package apptive.team5.alarm.event;

import apptive.team5.alarm.entity.AlarmMessage;

public record AlarmCreatedEvent(
        Long receiverId,
        AlarmMessage alarmMessage,
        String content,
        String deepLink
) {
}
