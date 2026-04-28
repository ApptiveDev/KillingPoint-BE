package apptive.team5.alarm.service;

import apptive.team5.alarm.dto.AlarmResponse;
import apptive.team5.alarm.entity.Alarm;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmLowService alarmLowService;

    @Transactional(readOnly = true)
    public Page<AlarmResponse> getAlarms(Long userId, Pageable pageable) {

        return alarmLowService.findByUserIdWithPage(userId, pageable)
                .map(AlarmResponse::new);
    }

    public Alarm save(Alarm alarm) {
        return alarmLowService.save(alarm);
    }
}
