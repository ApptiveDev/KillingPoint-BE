package apptive.team5.comment.controller;

import apptive.team5.comment.dto.CommentCreateRequest;
import apptive.team5.comment.dto.CommentPageResponse;
import apptive.team5.comment.dto.CommentResponse;
import apptive.team5.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries/{diaryId}/comments")
public class DiaryCommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<CommentPageResponse> getComments(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        CommentPageResponse response = commentService.getComments(diaryId, userId, PageRequest.of(page, size));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        CommentResponse response = commentService.createComment(diaryId, userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/comments/" + response.commentId()))
                .body(response);
    }
}
