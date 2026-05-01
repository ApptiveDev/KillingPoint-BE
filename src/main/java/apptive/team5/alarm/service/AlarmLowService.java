package apptive.team5.alarm.service;

import apptive.team5.alarm.entity.Alarm;
import apptive.team5.alarm.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AlarmLowService {

    private final AlarmRepository alarmRepository;

    @Transactional(readOnly = true)
    public Page<Alarm> findByUserIdWithPage(Long userId, Pageable pageable) {
        return alarmRepository.findByUserIdWithPage(userId, pageable);
    }


    public Alarm save(Alarm alarm) {
        return alarmRepository.save(alarm);
    }

    public void deleteByUserId(Long userId) {
        alarmRepository.deleteByUserId(userId);
    }
}
