package apptive.team5.alarmv2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlarmDispatchV2Scheduler {

    private final AlarmDispatchV2Service alarmDispatchV2Service;

    @Scheduled(fixedDelay = 15000)
    public void dispatchDueAlarms() {
        alarmDispatchV2Service.processDueScheduledDispatches();
    }
}
