package apptive.team5.comment.repository;

import apptive.team5.comment.domain.CommentEntity;
import apptive.team5.comment.domain.CommentStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static apptive.team5.comment.domain.QCommentEntity.commentEntity;
import static apptive.team5.user.domain.QUserEntity.userEntity;

@Transactional
@Repository
public class QCommentRepository {

    private final JPAQueryFactory queryFactory;

    public QCommentRepository(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    public List<CommentEntity> findRecentActiveTopLevelComments(Long diaryId, Set<Long> excludedUserIds, int limit) {
        return queryFactory
                .selectFrom(commentEntity)
                .join(commentEntity.user, userEntity).fetchJoin()
                .where(
                        commentEntity.diary.id.eq(diaryId),
                        commentEntity.parent.isNull(),
                        commentEntity.status.eq(CommentStatus.ACTIVE),
                        notInExcludedUserIds(excludedUserIds)
                )
                .orderBy(commentEntity.id.desc())
                .limit(limit)
                .fetch();
    }

    private BooleanExpression notInExcludedUserIds(Set<Long> excludedUserIds) {
        if (excludedUserIds == null || excludedUserIds.isEmpty()) {
            return null;
        }
        return commentEntity.user.id.notIn(excludedUserIds);
    }
}
