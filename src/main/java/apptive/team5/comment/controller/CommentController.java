package apptive.team5.comment.controller;

import apptive.team5.comment.dto.CommentResponse;
import apptive.team5.comment.dto.CommentUpdateRequest;
import apptive.team5.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments/{commentId}")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/replies")
    public ResponseEntity<Page<CommentResponse>> getReplies(
            @PathVariable Long commentId,
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<CommentResponse> response = commentService.getReplies(commentId, userId, PageRequest.of(page, size));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        CommentResponse response = commentService.updateComment(commentId, userId, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal Long userId
    ) {
        commentService.deleteComment(commentId, userId);

        return ResponseEntity.noContent().build();
    }
}
