package apptive.team5.comment.service;

import apptive.team5.comment.domain.CommentMentionEntity;
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

    public List<CommentMentionEntity> saveAll(List<CommentMentionEntity> mentions) {
        return commentMentionRepository.saveAll(mentions);
    }

    @Transactional(readOnly = true)
    public List<CommentMentionEntity> findByCommentIds(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return List.of();
        }
        return commentMentionRepository.findByCommentIds(commentIds);
    }

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
