package apptive.team5.alarm.dto;


import apptive.team5.alarm.entity.Alarm;

import java.time.LocalDateTime;

public record AlarmResponse(
        Long alarmId,
        String title,
        String content,
        String deepLink,
        LocalDateTime createDate
) {

    public AlarmResponse(Alarm alarm) {
        this(
                alarm.getId(),
                alarm.getTitle(),
                alarm.getContent(),
                alarm.getDeepLink(),
                alarm.getCreateDateTime()
        );
    }
}
