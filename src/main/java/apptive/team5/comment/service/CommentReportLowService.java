package apptive.team5.comment.service;

import apptive.team5.comment.domain.CommentEntity;
import apptive.team5.comment.domain.CommentReportEntity;
import apptive.team5.comment.repository.CommentReportRepository;
import apptive.team5.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentReportLowService {

    private final CommentReportRepository commentReportRepository;

    public CommentReportEntity save(CommentReportEntity commentReport) {
        return commentReportRepository.save(commentReport);
    }

    @Transactional(readOnly = true)
    public boolean existsByUserAndComment(UserEntity user, CommentEntity comment) {
        return commentReportRepository.existsByUserAndComment(user, comment);
    }

    public void deleteByCommentIds(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return;
        }
        commentReportRepository.deleteByCommentIds(commentIds);
    }

    public void deleteByUserId(Long userId) {
        commentReportRepository.deleteByUserId(userId);
    }
}
