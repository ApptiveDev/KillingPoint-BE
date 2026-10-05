package apptive.team5.comment.service;

import apptive.team5.comment.domain.CommentEntity;
import apptive.team5.comment.domain.CommentLikeEntity;
import apptive.team5.comment.repository.CommentLikeRepository;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.global.exception.NotFoundEntityException;
import apptive.team5.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentLikeLowService {

    private final CommentLikeRepository commentLikeRepository;

    public CommentLikeEntity save(CommentLikeEntity commentLike) {
        return commentLikeRepository.save(commentLike);
    }

    public void delete(CommentLikeEntity commentLike) {
        commentLikeRepository.delete(commentLike);
    }

    @Transactional(readOnly = true)
    public boolean existsByUserAndComment(UserEntity user, CommentEntity comment) {
        return commentLikeRepository.existsByUserAndComment(user, comment);
    }

    @Transactional(readOnly = true)
    public CommentLikeEntity findByUserAndComment(UserEntity user, CommentEntity comment) {
        return commentLikeRepository.findByUserAndComment(user, comment)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.NOT_FOUND_COMMENT_LIKE.getDescription()));
    }

    @Transactional(readOnly = true)
    public Set<Long> findLikedCommentIds(Long userId, List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return Set.of();
        }
        return commentLikeRepository.findLikedCommentIdsByUser(userId, commentIds);
    }

    public void deleteByCommentIds(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return;
        }
        commentLikeRepository.deleteByCommentIds(commentIds);
    }

    public void deleteByUserId(Long userId) {
        commentLikeRepository.deleteByUserId(userId);
    }
}
