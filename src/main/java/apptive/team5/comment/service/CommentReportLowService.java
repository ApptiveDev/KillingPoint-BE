package apptive.team5.comment.service;

import apptive.team5.comment.repository.CommentReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentReportLowService {

    private final CommentReportRepository commentReportRepository;

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
