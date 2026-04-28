package apptive.team5.alarm.event;

import apptive.team5.fcm.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AlarmEventListener {

    private final FcmService fcmService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAlarmCreated(AlarmCreatedEvent event) {
        fcmService.sendAlarm(event.receiverId(), event.title(), event.content());
    }
}
