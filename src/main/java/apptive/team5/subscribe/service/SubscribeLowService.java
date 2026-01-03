package apptive.team5.subscribe.service;

import apptive.team5.subscribe.domain.Subscribe;
import apptive.team5.subscribe.repository.SubscribeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class SubscribeLowService {

    private final SubscribeRepository subscribeRepository;

    public Subscribe save(Subscribe subscribe) {
        return subscribeRepository.save(subscribe);
    }

    public void deleteBySubscriberIdAndSubscribedToId(Long subscribedToId, Long subscriberId) {
        subscribeRepository.deleteBySubscriberIdAndSubscribedToId(subscribedToId, subscriberId);
    }

    @Transactional(readOnly = true)
    public List<Subscribe> findBySubscriberId(Long subscriberId) {
        return  subscribeRepository.findBySubscriberId(subscriberId);
    }

    @Transactional(readOnly = true)
    public Page<Subscribe> findBySubscriberIdWithSubscribedToPage(Long subscriberId, Pageable pageable) {
        return subscribeRepository.findBySubscriberIdWithSubscribedToPage(subscriberId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Subscribe> findBySubscribedToIdWithSubscriberToPage(Long subscribedToId, Pageable pageable) {
        return subscribeRepository.findBySubscribedToIdWithSubscriberPage(subscribedToId, pageable);
    }

    @Transactional(readOnly = true)
    public int countSubscriberBySubscribedToId(Long subscribedToId) {
        return subscribeRepository.countSubscriberBySubscribedToId(subscribedToId);
    }

    @Transactional(readOnly = true)
    public int countSubscribedTobySubscriberId(Long subscribedToId) {
        return subscribeRepository.countSubscribedTobySubscriberId(subscribedToId);
    }

    public void deleteByUserId(Long userId) {
        subscribeRepository.deleteByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Subscribe> findBySubscriberIdAndSubscribedToIds(Long subscriberId, List<Long> subscribedToIds) {
        return subscribeRepository.findBySubscriberIdAndSubscribedToIds(subscriberId, subscribedToIds);
    }

    @Transactional(readOnly = true)
    public boolean existsBySubscriberIdAndSubscribedToId(Long subscriberId, Long subscribedToId) {
        return subscribeRepository.existsBySubscriberIdAndSubscribedToId(subscriberId, subscribedToId);
    }
}
