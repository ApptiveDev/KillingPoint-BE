package apptive.team5.comment.service;

import apptive.team5.comment.domain.CommentEntity;
import apptive.team5.comment.domain.CommentStatus;
import apptive.team5.comment.dto.CommentReplyCountDto;
import apptive.team5.comment.repository.CommentRepository;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.global.exception.NotFoundEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentLowService {

    private final CommentRepository commentRepository;

    public CommentEntity save(CommentEntity comment) {
        return commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public CommentEntity findById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.NOT_FOUND_COMMENT.getDescription()));
    }

    @Transactional(readOnly = true)
    public CommentEntity findByIdWithUserAndDiary(Long commentId) {
        return commentRepository.findByIdWithUserAndDiary(commentId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.NOT_FOUND_COMMENT.getDescription()));
    }

    @Transactional(readOnly = true)
    public Page<CommentEntity> findTopLevelPageOrderByPopularity(Long diaryId, Pageable pageable) {
        Page<Long> idPage = commentRepository.findTopLevelIdsOrderByPopularity(diaryId, pageable);

        if (idPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> orderedIds = idPage.getContent();

        Map<Long, CommentEntity> commentMap = commentRepository.findAllByIdInWithUser(orderedIds)
                .stream()
                .collect(Collectors.toMap(CommentEntity::getId, Function.identity()));

        List<CommentEntity> orderedComments = orderedIds.stream()
                .map(commentMap::get)
                .filter(Objects::nonNull)
                .toList();

        return new PageImpl<>(orderedComments, pageable, idPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<CommentEntity> findActiveReplies(Long parentId, Pageable pageable) {
        return commentRepository.findRepliesByParentId(parentId, CommentStatus.ACTIVE, pageable);
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> findActiveReplyCounts(List<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return Map.of();
        }

        return commentRepository.countRepliesByParentIds(parentIds, CommentStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(
                        CommentReplyCountDto::parentCommentId,
                        CommentReplyCountDto::replyCount,
                        (a, b) -> a
                ));
    }

    @Transactional(readOnly = true)
    public long countActiveTopLevelByDiaryId(Long diaryId) {
        return commentRepository.countTopLevelByDiaryId(diaryId, CommentStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Long> findIdsByDiaryIds(List<Long> diaryIds) {
        if (diaryIds == null || diaryIds.isEmpty()) {
            return List.of();
        }
        return commentRepository.findIdsByDiaryIds(diaryIds);
    }

    @Transactional(readOnly = true)
    public List<Long> findIdsByUserId(Long userId) {
        return commentRepository.findIdsByUserId(userId);
    }

    public void deleteByDiaryIds(List<Long> diaryIds) {
        if (diaryIds == null || diaryIds.isEmpty()) {
            return;
        }
        commentRepository.deleteRepliesByDiaryIds(diaryIds);
        commentRepository.deleteTopLevelByDiaryIds(diaryIds);
    }

    public void increaseLikeCount(Long commentId) {
        commentRepository.increaseLikeCount(commentId);
    }

    public void decreaseLikeCount(Long commentId) {
        commentRepository.decreaseLikeCount(commentId);
    }

    public void decrementLikeCountForUserLikes(Long userId) {
        commentRepository.decrementLikeCountForUserLikes(userId);
    }

    public void markDeletedAndDetachUser(Long userId) {
        commentRepository.markDeletedAndDetachUser(userId, CommentStatus.DELETED);
    }
}
