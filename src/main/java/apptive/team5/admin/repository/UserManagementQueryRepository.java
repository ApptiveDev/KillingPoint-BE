package apptive.team5.admin.repository;

import apptive.team5.admin.dto.UserSearchType;
import apptive.team5.diary.domain.DiaryEntity;
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

import static apptive.team5.diary.domain.QDiaryEntity.diaryEntity;
import static apptive.team5.diary.domain.QDiaryReportEntity.diaryReportEntity;
import static apptive.team5.user.domain.QUserEntity.userEntity;

@Repository
@Transactional(readOnly = true)
public class UserManagementQueryRepository {

    private final JPAQueryFactory queryFactory;

    public UserManagementQueryRepository(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    public Page<UserEntity> findUsers(UserSearchType searchType, String query, Pageable pageable) {
        BooleanExpression searchCondition = searchCondition(searchType, query);

        List<UserEntity> content = queryFactory
                .selectFrom(userEntity)
                .where(searchCondition)
                .orderBy(userEntity.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(userEntity.count())
                .from(userEntity)
                .where(searchCondition);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    public Page<DiaryEntity> findUserDiaries(Long userId, boolean reportedOnly, Pageable pageable) {
        BooleanExpression userCondition = diaryEntity.user.id.eq(userId);
        BooleanExpression reportCondition = reportedOnlyCondition(reportedOnly);

        List<DiaryEntity> content = queryFactory
                .selectFrom(diaryEntity)
                .where(userCondition, reportCondition)
                .orderBy(diaryEntity.createDateTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(diaryEntity.count())
                .from(diaryEntity)
                .where(userCondition, reportCondition);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression reportedOnlyCondition(boolean reportedOnly) {
        if (!reportedOnly) {
            return null;
        }

        return JPAExpressions
                .selectOne()
                .from(diaryReportEntity)
                .where(diaryReportEntity.diary.eq(diaryEntity))
                .exists();
    }

    private BooleanExpression searchCondition(UserSearchType searchType, String query) {
        if (query == null || query.isBlank()) {
            return null;
        }

        return switch (searchType) {
            case USER_ID -> userIdCondition(query);
            case EMAIL -> userEntity.email.lower().contains(query);
            case TAG -> userEntity.tag.lower().contains(query);
            case USERNAME -> userEntity.username.lower().contains(query);
        };
    }

    private BooleanExpression userIdCondition(String query) {
        try {
            return userEntity.id.eq(Long.parseLong(query));
        } catch (NumberFormatException e) {
            return userEntity.id.eq(-1L);
        }
    }
}
