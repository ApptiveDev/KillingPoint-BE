package apptive.team5.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentCleanupService {

    private final CommentLowService commentLowService;
    private final CommentLikeLowService commentLikeLowService;
    private final CommentMentionLowService commentMentionLowService;
    private final CommentReportLowService commentReportLowService;

    public void deleteByDiaryId(Long diaryId) {
        deleteByDiaryIds(List.of(diaryId));
    }

    public void deleteByDiaryIds(List<Long> diaryIds) {
        if (diaryIds == null || diaryIds.isEmpty()) {
            return;
        }

        List<Long> commentIds = commentLowService.findIdsByDiaryIds(diaryIds);

        commentReportLowService.deleteByCommentIds(commentIds);
        commentLikeLowService.deleteByCommentIds(commentIds);
        commentMentionLowService.deleteByCommentIds(commentIds);
        commentLowService.deleteByDiaryIds(diaryIds);
    }

    public void handleUserWithdrawal(Long userId) {
        commentLowService.decrementLikeCountForUserLikes(userId);
        commentLikeLowService.deleteByUserId(userId);
        commentReportLowService.deleteByUserId(userId);
        commentMentionLowService.deleteByTargetUserId(userId);
        commentLowService.markDeletedAndDetachUser(userId);
    }
}
