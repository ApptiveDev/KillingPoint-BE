package apptive.team5.alarmv2.dto;

import apptive.team5.user.domain.UserEntity;

public record AlarmV2RecipientItem(Long userId, String username, String tag, boolean selectable, String statusLabel, String exclusionReason) {
    public static AlarmV2RecipientItem from(UserEntity user) {
        if (user.isLocked()) return new AlarmV2RecipientItem(user.getId(), user.getUsername(), user.getTag(), false, "발송 제외", "정지 유저");
        if (!user.isAlarmEnabled()) return new AlarmV2RecipientItem(user.getId(), user.getUsername(), user.getTag(), false, "발송 제외", "푸시 수신 설정 꺼짐");
        return new AlarmV2RecipientItem(user.getId(), user.getUsername(), user.getTag(), true, "발송 가능", "");
    }
}
