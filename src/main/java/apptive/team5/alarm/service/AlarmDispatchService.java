package apptive.team5.alarm.service;

import apptive.team5.alarm.dto.AlarmSendRequest;
import apptive.team5.alarm.entity.Alarm;
import apptive.team5.alarm.entity.AlarmMessage;
import apptive.team5.alarm.event.AlarmCreatedEvent;
import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.service.DiaryLowService;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AlarmDispatchService {

    private final AlarmLowService alarmLowService;
    private final DiaryLowService diaryLowService;
    private final UserLowService userLowService;
    private final ApplicationEventPublisher eventPublisher;

    @Async("sendAlarm")
    public void saveAndDispatchForLike(AlarmSendRequest request) {

        Long actorId = request.actorId();
        UserEntity actor = userLowService.findById(actorId);
        DiaryEntity diary = diaryLowService.findByIdWithUser(request.diaryId());
        UserEntity receiver = diary.getUser();

        String title = AlarmMessage.LIKE_ALARM.getMessage();
        String content = actor.getUsername() + "님이 회원님의 킬링파트를 좋아합니다.";

        alarmLowService.save(new Alarm(
                title,
                content,
                receiver
        ));

        eventPublisher.publishEvent(new AlarmCreatedEvent(
                receiver.getId(),
                title,
                content
        ));
    }
}
