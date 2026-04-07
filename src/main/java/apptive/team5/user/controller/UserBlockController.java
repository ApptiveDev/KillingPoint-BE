package apptive.team5.user.controller;

import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.dto.UserResponse;
import apptive.team5.user.service.UserBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserBlockController {

    private final UserBlockService userBlockService;

    @PostMapping("/{blockedId}/blocks")
    public ResponseEntity<Void> addBlockedUser(
            @PathVariable Long blockedId,
            @AuthenticationPrincipal Long userId) {

        userBlockService.addBlockedUser(blockedId, userId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{blockedId}/blocks")
    public ResponseEntity<Void> removeBlockedUser(
            @PathVariable Long blockedId,
            @AuthenticationPrincipal Long userId){

        userBlockService.removeBlockedUser(userId, blockedId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/blocks")
    public ResponseEntity<Page<UserResponse>> getBlockedUser(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
            ) {


        Page<UserResponse> response = userBlockService.getBlockedUser(userId, PageRequest.of(page, size));

        return ResponseEntity.status(HttpStatus.OK).body(response);

    }
}
