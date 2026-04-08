package apptive.team5.user.repository;

import apptive.team5.user.domain.UserEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static apptive.team5.diary.domain.QDiaryLikeEntity.diaryLikeEntity;
import static apptive.team5.user.domain.QUserEntity.userEntity;

@Transactional
@Repository
public class QUserRepository {

    private final JPAQueryFactory queryFactory;

    public QUserRepository(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    public Page<UserEntity> findByTagOrUsernameExcludingBlocked(Set<Long> blockedUserIds, String searchCond, Pageable pageable) {
        BooleanExpression searchCondition = tagLike(searchCond).or(usernameLike(searchCond));
        BooleanExpression notBlockedCondition = notInBlockedUserIds(blockedUserIds);

        List<UserEntity> content = queryFactory
                .selectFrom(userEntity)
                .where(searchCondition, notBlockedCondition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(userEntity.count())
                .from(userEntity)
                .where(searchCondition, notBlockedCondition);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression notInBlockedUserIds(Set<Long> blockedUserIds) {
        if (blockedUserIds == null || blockedUserIds.isEmpty()) {
            return null;
        }
        return userEntity.id.notIn(blockedUserIds);
    }

    private BooleanExpression tagLike(String tag) {
        return tag != null ? userEntity.tag.like("%" + tag + "%") : null;
    }

    private BooleanExpression usernameLike(String username) {
        return username != null ? userEntity.username.like("%" + username + "%") : null;
    }
}
