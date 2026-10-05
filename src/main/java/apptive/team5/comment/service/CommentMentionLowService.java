package apptive.team5.comment.service;

import apptive.team5.comment.domain.MentionTargetType;
import apptive.team5.comment.repository.CommentMentionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentMentionLowService {

    private final CommentMentionRepository commentMentionRepository;

    public void deleteByCommentIds(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return;
        }
        commentMentionRepository.deleteByCommentIds(commentIds);
    }

    public void deleteByTargetUserId(Long userId) {
        commentMentionRepository.deleteByTarget(MentionTargetType.USER, userId);
    }
}
