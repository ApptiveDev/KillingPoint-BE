package apptive.team5.alarm.dto;

import apptive.team5.user.domain.UserEntity;

public record AlarmSendRequest(
        Long diaryId,
        Long actorId
) {
}
