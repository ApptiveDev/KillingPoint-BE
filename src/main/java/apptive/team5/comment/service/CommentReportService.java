package apptive.team5.comment.service;

import apptive.team5.comment.domain.CommentEntity;
import apptive.team5.comment.domain.CommentReportEntity;
import apptive.team5.comment.dto.CommentReportRequestDto;
import apptive.team5.comment.dto.CommentReportResponseDto;
import apptive.team5.global.exception.DuplicateException;
import apptive.team5.global.exception.ExceptionCode;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentReportService {

    private final CommentReportLowService commentReportLowService;
    private final CommentLowService commentLowService;
    private final UserLowService userLowService;

    public CommentReportResponseDto createCommentReport(CommentReportRequestDto request, Long commentId, Long userId) {
        UserEntity user = userLowService.getReferenceById(userId);

        CommentEntity comment = commentLowService.findById(commentId);
        comment.validateActive();

        if (commentReportLowService.existsByUserAndComment(user, comment)) {
            throw new DuplicateException(ExceptionCode.DUPLICATE_COMMENT_REPORT.getDescription());
        }

        CommentReportEntity saved = commentReportLowService.save(
                new CommentReportEntity(request.content(), comment.getContent(), comment, user));

        return new CommentReportResponseDto(saved);
    }
}
