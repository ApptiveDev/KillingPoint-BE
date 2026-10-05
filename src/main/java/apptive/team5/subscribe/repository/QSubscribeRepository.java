package apptive.team5.subscribe.repository;

import apptive.team5.user.domain.UserEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static apptive.team5.subscribe.domain.QSubscribe.subscribe;
import static apptive.team5.user.domain.QUserEntity.userEntity;

@Transactional
@Repository
public class QSubscribeRepository {

    private final JPAQueryFactory queryFactory;

    public QSubscribeRepository(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    public List<UserEntity> findSubscribedUsersByKeyword(Long subscriberId, Set<Long> blockedUserIds, String keyword, int limit) {
        return queryFactory
                .select(userEntity)
                .from(subscribe)
                .join(subscribe.subscribedTo, userEntity)
                .where(
                        subscribe.subscriber.id.eq(subscriberId),
                        notInBlockedUserIds(blockedUserIds),
                        tagOrUsernameLike(keyword)
                )
                .orderBy(userEntity.username.asc(), userEntity.id.asc())
                .limit(limit)
                .fetch();
    }

    private BooleanExpression notInBlockedUserIds(Set<Long> blockedUserIds) {
        if (blockedUserIds == null || blockedUserIds.isEmpty()) {
            return null;
        }
        return userEntity.id.notIn(blockedUserIds);
    }

    private BooleanExpression tagOrUsernameLike(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String pattern = "%" + keyword.trim() + "%";
        return userEntity.tag.like(pattern).or(userEntity.username.like(pattern));
    }
}
