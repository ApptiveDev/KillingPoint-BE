package apptive.team5.comment.service;

import apptive.team5.comment.repository.CommentLikeRepository;
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
