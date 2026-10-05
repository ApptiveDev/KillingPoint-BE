package apptive.team5.comment.controller;

import apptive.team5.comment.dto.MentionCandidateResponse;
import apptive.team5.comment.service.CommentMentionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments/mention-candidates")
public class CommentMentionController {

    private final CommentMentionService commentMentionService;

    @GetMapping
    public ResponseEntity<List<MentionCandidateResponse>> getMentionCandidates(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<MentionCandidateResponse> response = commentMentionService.getMentionCandidates(userId, keyword, size);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
