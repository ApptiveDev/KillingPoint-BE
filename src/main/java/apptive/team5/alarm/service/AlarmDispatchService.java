package apptive.team5.alarm.service;

import apptive.team5.alarm.dto.DiaryCreateAlarmSendRequest;
import apptive.team5.alarm.dto.DiaryLikeAlarmSendRequest;
import apptive.team5.alarm.dto.SubscribeAlarmSendRequest;
import apptive.team5.alarm.entity.Alarm;
import apptive.team5.alarm.entity.AlarmMessage;
import apptive.team5.alarm.event.AlarmCreatedEvent;
import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.service.DiaryLowService;
import apptive.team5.subscribe.domain.Subscribe;
import apptive.team5.subscribe.service.SubscribeLowService;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AlarmDispatchService {

    private static final String DIARY_DEEP_LINK_FORMAT = "/api/diaries/%d";
    private static final String SUBSCRIBE_DEEP_LINK_FORMAT = "/api/subscribes/%d/fans";

    private final AlarmLowService alarmLowService;
    private final DiaryLowService diaryLowService;
    private final UserLowService userLowService;
    private final ApplicationEventPublisher eventPublisher;
    private final SubscribeLowService subscribeLowService;

    @Async("sendAlarm")
    public void saveAndDispatchForLike(DiaryLikeAlarmSendRequest request) {

        Long actorId = request.actorId();
        UserEntity actor = userLowService.findById(actorId);
        DiaryEntity diary = diaryLowService.findByIdWithUser(request.diaryId());
        UserEntity receiver = diary.getUser();

        String title = AlarmMessage.LIKE_ALARM.getMessage();
        String content = actor.getUsername() + "님이 회원님의 킬링파트를 좋아합니다.";
        String deepLink = DIARY_DEEP_LINK_FORMAT.formatted(diary.getId());

        saveAndPublish(receiver, AlarmMessage.LIKE_ALARM, content, deepLink);
    }

    @Async("sendAlarm")
    public void saveAndDispatchForSubscribe(SubscribeAlarmSendRequest request) {
        UserEntity subscriber = userLowService.findById(request.subscriberId());
        UserEntity receiver = userLowService.findById(request.subscribedToUserId());

        String title = AlarmMessage.SUBSCRIBE_ALARM.getMessage();
        String content = subscriber.getUsername() + "님이 회원님을 픽했어요.";
        String deepLink = SUBSCRIBE_DEEP_LINK_FORMAT.formatted(receiver.getId());

        saveAndPublish(receiver, AlarmMessage.SUBSCRIBE_ALARM, content, deepLink);
    }

    @Async("sendAlarm")
    public void saveAndDispatchForDiaryCreate(DiaryCreateAlarmSendRequest request) {
        UserEntity actor = userLowService.findById(request.actorId());
        List<Subscribe> subscribers = subscribeLowService.findBySubscribedToId(actor.getId());

        String content = actor.getUsername() + "님이 새 킬링파트를 등록했어요.";
        String deepLink = DIARY_DEEP_LINK_FORMAT.formatted(request.diaryId());


        subscribers.forEach(subscriber ->
                saveAndPublish(subscriber.getSubscriber(), AlarmMessage.DIARY_ALARM, content, deepLink));
    }

    private void saveAndPublish(UserEntity receiver, AlarmMessage alarmMessage, String content, String deepLink) {
        alarmLowService.save(new Alarm(
                content,
                deepLink,
                receiver,
                alarmMessage
        ));

        eventPublisher.publishEvent(new AlarmCreatedEvent(
                receiver.getId(),
                alarmMessage,
                content,
                deepLink
        ));
    }
}
