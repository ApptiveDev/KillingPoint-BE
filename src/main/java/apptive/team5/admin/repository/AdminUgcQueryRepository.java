package apptive.team5.admin.repository;

import apptive.team5.admin.dto.AdminUgcSearchType;
import apptive.team5.diary.domain.DiaryEntity;
import apptive.team5.diary.domain.DiaryMemoEntity;
import apptive.team5.diary.domain.DiaryScope;
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
import static apptive.team5.diary.domain.QDiaryMemoEntity.diaryMemoEntity;
import static apptive.team5.diary.domain.QDiaryReportEntity.diaryReportEntity;
import static apptive.team5.user.domain.QUserEntity.userEntity;

@Repository
@Transactional(readOnly = true)
public class AdminUgcQueryRepository {

    private final JPAQueryFactory queryFactory;

    public AdminUgcQueryRepository(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    public Page<DiaryEntity> findPublicUgcItems(boolean reportedOnly,
                                                AdminUgcSearchType searchType,
                                                String query,
                                                Pageable pageable) {
        BooleanExpression scopeCondition = diaryEntity.scope.eq(DiaryScope.PUBLIC);
        BooleanExpression reportCondition = reportedOnlyCondition(reportedOnly);
        BooleanExpression searchCondition = searchCondition(searchType, query);

        List<DiaryEntity> content = queryFactory
                .selectFrom(diaryEntity)
                .join(diaryEntity.user, userEntity).fetchJoin()
                .where(scopeCondition, reportCondition, searchCondition)
                .orderBy(diaryEntity.createDateTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(diaryEntity.count())
                .from(diaryEntity)
                .join(diaryEntity.user, userEntity)
                .where(scopeCondition, reportCondition, searchCondition);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    public Page<DiaryMemoEntity> findMemoItems(Pageable pageable) {
        List<DiaryMemoEntity> content = queryFactory
                .selectFrom(diaryMemoEntity)
                .join(diaryMemoEntity.diary, diaryEntity).fetchJoin()
                .join(diaryEntity.user, userEntity).fetchJoin()
                .orderBy(diaryMemoEntity.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(diaryMemoEntity.count())
                .from(diaryMemoEntity);

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

    private BooleanExpression searchCondition(AdminUgcSearchType searchType, String query) {
        if (query == null || query.isBlank()) {
            return null;
        }

        return switch (searchType) {
            case DIARY_ID -> diaryIdCondition(query);
            case MUSIC_TITLE -> diaryEntity.musicTitle.lower().contains(query);
            case ARTIST -> diaryEntity.artist.lower().contains(query);
            case USERNAME -> userEntity.username.lower().contains(query);
        };
    }

    private BooleanExpression diaryIdCondition(String query) {
        try {
            return diaryEntity.id.eq(Long.parseLong(query));
        } catch (NumberFormatException e) {
            return diaryEntity.id.eq(-1L);
        }
    }
}
