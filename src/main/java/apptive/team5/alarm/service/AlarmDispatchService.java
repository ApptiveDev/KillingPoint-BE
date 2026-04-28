package apptive.team5.alarm.service;

import apptive.team5.alarm.dto.AlarmSendRequest;
import apptive.team5.alarm.entity.Alarm;
import apptive.team5.alarm.event.AlarmCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AlarmDispatchService {

    private final AlarmLowService alarmLowService;
    private final ApplicationEventPublisher eventPublisher;

    public void saveAndDispatch(AlarmSendRequest request) {
        alarmLowService.save(new Alarm(
                request.title(),
                request.content(),
                request.receiver()
        ));

        eventPublisher.publishEvent(new AlarmCreatedEvent(
                request.receiver().getId(),
                request.title(),
                request.content()
        ));
    }
}
