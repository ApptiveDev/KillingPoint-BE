package apptive.team5.alarm.dto;

import java.util.List;

public record AdminAlarmSendRequest(
        String content,
        String deepLink,
        List<Long> userIds,
        boolean isBroadCast
) {
}
