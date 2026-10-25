package apptive.team5.alarmv2.dto;

import apptive.team5.alarmv2.domain.AlarmDispatchV2;
import apptive.team5.alarmv2.domain.AlarmDispatchV2Status;
import apptive.team5.alarmv2.domain.AlarmDispatchV2Type;

import java.time.format.DateTimeFormatter;

public record AlarmDispatchV2HistoryItem(String title, int recipientCount, String displayTime, String statusLabel) {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    public static AlarmDispatchV2HistoryItem from(AlarmDispatchV2 dispatch) {
        String statusLabel = switch (dispatch.getStatus()) {
            case SCHEDULED -> "예약";
            case COMPLETED -> "발송 요청 완료";
            case FAILED -> "실패";
            case CANCELLED -> "취소";
            case PROCESSING -> "처리 중";
            case DRAFT -> "임시";
        };
        var dateTime = dispatch.getDispatchType() == AlarmDispatchV2Type.SCHEDULED
                ? dispatch.getScheduledAt()
                : dispatch.getCreateDateTime();
        if (dateTime == null) dateTime = dispatch.getCreateDateTime();
        String displayTime = dateTime == null ? "—" : DATE_FORMAT.format(dateTime);
        return new AlarmDispatchV2HistoryItem(dispatch.getTitle(), dispatch.getRecipientCount(), displayTime, statusLabel);
    }
}
