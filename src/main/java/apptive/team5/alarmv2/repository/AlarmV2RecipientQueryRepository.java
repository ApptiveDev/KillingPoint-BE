package apptive.team5.alarmv2.repository;

import apptive.team5.admin.dto.UserSearchType;
import apptive.team5.admin.repository.UserManagementQueryRepository;
import apptive.team5.user.domain.UserEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static apptive.team5.user.domain.QUserEntity.userEntity;

@Repository
@Transactional(readOnly = true)
public class AlarmV2RecipientQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final UserManagementQueryRepository userManagementQueryRepository;

    public AlarmV2RecipientQueryRepository(EntityManager entityManager,
                                           UserManagementQueryRepository userManagementQueryRepository) {
        this.queryFactory = new JPAQueryFactory(entityManager);
        this.userManagementQueryRepository = userManagementQueryRepository;
    }

    public Page<UserEntity> findRecipients(UserSearchType searchType, String query, boolean includeExcluded, Pageable pageable) {
        if (includeExcluded) {
            return userManagementQueryRepository.findUsers(searchType, query, false, pageable);
        }

        BooleanExpression search = searchCondition(searchType, query);
        BooleanExpression eligible = userEntity.alarmEnabled.isTrue().and(userEntity.locked.isFalse());
        List<UserEntity> content = queryFactory.selectFrom(userEntity).where(search, eligible).orderBy(userEntity.id.desc()).offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();
        JPAQuery<Long> countQuery = queryFactory.select(userEntity.count()).from(userEntity).where(search, eligible);
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    public long countSelectableRecipients(UserSearchType searchType, String query) {
        return queryFactory.select(userEntity.count())
                .from(userEntity)
                .where(searchCondition(searchType, query), userEntity.alarmEnabled.isTrue(), userEntity.locked.isFalse())
                .fetchOne();
    }

    public Page<Long> findSelectableRecipientIds(Pageable pageable) {
        List<Long> ids = queryFactory.select(userEntity.id)
                .from(userEntity)
                .where(userEntity.alarmEnabled.isTrue(), userEntity.locked.isFalse())
                .orderBy(userEntity.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        JPAQuery<Long> countQuery = queryFactory.select(userEntity.count())
                .from(userEntity)
                .where(userEntity.alarmEnabled.isTrue(), userEntity.locked.isFalse());
        return PageableExecutionUtils.getPage(ids, pageable, countQuery::fetchOne);
    }

    private BooleanExpression searchCondition(UserSearchType type, String query) {
        if (query == null || query.isBlank()) return null;
        String value = query.trim().toLowerCase();
        return switch (type) {
            case USER_ID -> userIdCondition(value);
            case USERNAME -> userEntity.username.lower().contains(value);
            case EMAIL -> userEntity.email.lower().contains(value);
            case TAG -> userEntity.tag.lower().contains(value);
        };
    }

    private BooleanExpression userIdCondition(String query) {
        try {
            return userEntity.id.eq(Long.parseLong(query));
        } catch (NumberFormatException exception) {
            return userEntity.id.eq(-1L);
        }
    }
}
