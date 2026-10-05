package apptive.team5.comment.service;

import apptive.team5.comment.domain.CommentEntity;
import apptive.team5.comment.domain.CommentLikeEntity;
import apptive.team5.comment.dto.CommentLikeResponse;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentLikeService {

    private final CommentLikeLowService commentLikeLowService;
    private final CommentLowService commentLowService;
    private final UserLowService userLowService;

    public CommentLikeResponse toggleCommentLike(Long userId, Long commentId) {
        CommentEntity comment = commentLowService.findById(commentId);
        comment.validateActive();

        UserEntity user = userLowService.getReferenceById(userId);

        boolean isLiked;
        if (commentLikeLowService.existsByUserAndComment(user, comment)) {
            CommentLikeEntity commentLike = commentLikeLowService.findByUserAndComment(user, comment);
            commentLikeLowService.delete(commentLike);
            commentLowService.decreaseLikeCount(commentId);
            isLiked = false;
        } else {
            commentLikeLowService.save(new CommentLikeEntity(user, comment));
            commentLowService.increaseLikeCount(commentId);
            isLiked = true;
        }

        long likeCount = commentLowService.findById(commentId).getLikeCount();

        return new CommentLikeResponse(isLiked, likeCount);
    }
}
