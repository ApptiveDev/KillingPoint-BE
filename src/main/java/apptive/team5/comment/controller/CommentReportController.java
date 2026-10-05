package apptive.team5.comment.controller;

import apptive.team5.comment.dto.CommentReportRequestDto;
import apptive.team5.comment.dto.CommentReportResponseDto;
import apptive.team5.comment.service.CommentReportService;
import apptive.team5.mail.service.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments/{commentId}/reports")
public class CommentReportController {

    private final CommentReportService commentReportService;
    private final MailService mailService;

    @PostMapping
    public ResponseEntity<Void> reportComment(
            @Valid @RequestBody CommentReportRequestDto request,
            @PathVariable Long commentId,
            @AuthenticationPrincipal Long userId
    ) {
        CommentReportResponseDto response = commentReportService.createCommentReport(request, commentId, userId);
        mailService.sendCommentReportedMailMessage(response);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
