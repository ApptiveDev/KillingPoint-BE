package apptive.team5.alarm.dto;


import apptive.team5.alarm.entity.Alarm;

public record AlarmResponse(
        Long alarmId,
        String title,
        String content
) {

    public AlarmResponse(Alarm alarm) {
        this(
                alarm.getId(),
                alarm.getTitle(),
                alarm.getContent()
        );
    }
}
