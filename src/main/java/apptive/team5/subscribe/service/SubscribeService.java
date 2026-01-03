package apptive.team5.subscribe.service;


import apptive.team5.global.exception.BadRequestException;
import apptive.team5.global.exception.DuplicateException;
import apptive.team5.subscribe.domain.Subscribe;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.dto.UserResponse;
import apptive.team5.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static apptive.team5.global.exception.ExceptionCode.BAD_SUBSCRIBE_REQUEST;
import static apptive.team5.global.exception.ExceptionCode.DUPLICATE_SUBSCRIBE_REQUEST;

@Transactional
@RequiredArgsConstructor
@Service
public class SubscribeService {

    private final SubscribeLowService subscribeLowService;
    private final UserLowService userLowService;

    public void save(Long subscribeToUserId, Long subscriberId) {

        if (subscribeToUserId.equals(subscriberId))
            throw new BadRequestException(BAD_SUBSCRIBE_REQUEST.getDescription());

        if(subscribeLowService.existsBySubscriberIdAndSubscribedToId(subscriberId, subscribeToUserId))
            throw new DuplicateException(DUPLICATE_SUBSCRIBE_REQUEST.getDescription());


        UserEntity subscribedTo = userLowService.findById(subscribeToUserId);
        UserEntity subscriber = userLowService.getReferenceById(subscriberId);

        Subscribe subscribe = new Subscribe(subscriber, subscribedTo);

        subscribeLowService.save(subscribe);
    }

    public void deleteBySubscriberIdAndSubscribedToId(Long subscriberToUserId, Long subscriberId) {
        subscribeLowService.deleteBySubscriberIdAndSubscribedToId(subscriberToUserId, subscriberId);
    }

    public Page<UserResponse> findMySubscribedUsers(Long subscriberId, Pageable pageable) {

       return subscribeLowService.findBySubscriberIdWithSubscribedToPage(subscriberId, pageable)
                .map(subscribe -> new UserResponse(subscribe.getSubscribedTo()));
    }

    public Page<UserResponse> findMySubscriberUsers(Long subscribedToId, Pageable pageable) {

        return subscribeLowService.findBySubscribedToIdWithSubscriberToPage(subscribedToId, pageable)
                .map(subscribe -> new UserResponse(subscribe.getSubscriber()));
    }
}

