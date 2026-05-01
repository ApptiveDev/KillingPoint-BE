package apptive.team5.alarm.entity;

import lombok.Getter;

@Getter
public enum AlarmMessage {

    LIKE_ALARM("킬링파트에 좋아요가 도착했어요");

    private final String message;


    AlarmMessage(String message) {
        this.message = message;
    }
}
