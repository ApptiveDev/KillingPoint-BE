package apptive.team5.alarm.entity;

import lombok.Getter;

@Getter
public enum AlarmMessage {

    LIKE_ALARM("킬링파트에 좋아요가 도착했어요"),
    SUBSCRIBE_ALARM("새로운 구독자가 생겼어요");

    private final String message;


    AlarmMessage(String message) {
        this.message = message;
    }
}
