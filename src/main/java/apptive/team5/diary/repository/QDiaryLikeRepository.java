package apptive.team5.diary.repository;

import apptive.team5.diary.domain.DiaryLikeEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static apptive.team5.diary.domain.QDiaryLikeEntity.*;
import static apptive.team5.user.domain.QUserEntity.userEntity;

@Transactional
@Repository
public class QDiaryLikeRepository {

    private final JPAQueryFactory queryFactory;

    public QDiaryLikeRepository(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    public Page<DiaryLikeEntity> findByDiaryIdLikeSearchCond(Long diaryId, String searchCond, Pageable pageable) {

        List<DiaryLikeEntity> content = queryFactory
                .selectFrom(diaryLikeEntity)
                .join(diaryLikeEntity.user, userEntity).fetchJoin()
                .where(
                        diaryLikeEntity.diary.id.eq(diaryId),
                        searchTagOrUserName(searchCond)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(diaryLikeEntity.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(diaryLikeEntity.count())
                .join(diaryLikeEntity.user, userEntity)
                .where(
                        diaryLikeEntity.diary.id.eq(diaryId),
                        searchTagOrUserName(searchCond)
                )
                .from(diaryLikeEntity);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression searchTagOrUserName(String searchCond) {

        if (searchCond == null) {
            return null;
        }

        return userEntity.tag.like("%" + searchCond + "%")
                .or(userEntity.username.like("%" + searchCond + "%"));
    }
}
