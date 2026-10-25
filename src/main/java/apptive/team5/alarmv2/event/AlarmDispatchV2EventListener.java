package apptive.team5.alarmv2.event;

import apptive.team5.fcm.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AlarmDispatchV2EventListener {

    private final FcmService fcmService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AlarmDispatchV2CreatedEvent event) {
        fcmService.sendAdminAlarmV2(event.recipientId(), event.title(), event.content(), event.deepLink());
    }
}
