package apptive.team5.subscribe.repository;

import apptive.team5.subscribe.domain.Subscribe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubscribeRepository extends JpaRepository<Subscribe, Long> {

    @Query("select s from Subscribe s where s.subscriber.id = :subscriberId")
    List<Subscribe> findBySubscriberId(Long subscriberId);

    @Query(
            value = "select s from Subscribe s join fetch s.subscribedTo where s.subscriber.id = :subscriberId",
            countQuery = "select count(s.id) from Subscribe s where s.subscriber.id = :subscriberId "
    )
    Page<Subscribe> findBySubscriberIdWithSubscribedToPage(Long subscriberId, Pageable pageable);

    @Query(
            value = "select s from Subscribe s join fetch s.subscriber where s.subscribedTo.id = :subscribedToId",
            countQuery = "select count(s.id) from Subscribe s where s.subscribedTo.id = :subscribedToId "
    )
    Page<Subscribe> findBySubscribedToIdWithSubscriberPage(Long subscribedToId, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("delete from Subscribe s where s.subscribedTo.id = :subscribedToId and s.subscriber.id = :subscriberId")
    void deleteBySubscriberIdAndSubscribedToId(Long subscribedToId, Long subscriberId);

    @Query("select count(s) from Subscribe s where s.subscribedTo.id = :subscribedToId")
    int countSubscriberBySubscribedToId(Long subscribedToId);

    @Query("select count(s) from Subscribe s where s.subscriber.id = :subscriberId")
    int countSubscribedTobySubscriberId(Long subscriberId);

    @Modifying(clearAutomatically = true)
    @Query("delete from Subscribe s where s.subscriber.id = :userId or s.subscribedTo.id = :userId")
    void deleteByUserId(Long userId);

    @Query("""
            select s from Subscribe s
            where s.subscriber.id = :subscriberId and
            s.subscribedTo.id in :subscribedToIds
           """)
    List<Subscribe> findBySubscriberIdAndSubscribedToIds(Long subscriberId, List<Long> subscribedToIds);

    boolean existsBySubscriberIdAndSubscribedToId(Long subscriberId, Long subscribedToId);
}
