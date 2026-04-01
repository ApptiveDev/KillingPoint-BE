package apptive.team5.subscribe.controller;

import apptive.team5.subscribe.service.SubscribeService;
import apptive.team5.user.dto.UserResponse;
import apptive.team5.user.dto.UserSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscribes")
@RequiredArgsConstructor
public class SubscribeController {

    private final SubscribeService subscribeService;


    @PostMapping("/{subscribeToUserId}")
    public ResponseEntity<Void> subscribe(@PathVariable Long subscribeToUserId, @AuthenticationPrincipal Long userId) {

        subscribeService.save(subscribeToUserId, userId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{subscribeToUserId}")
    public ResponseEntity<Void> cancelSubscribe(@PathVariable Long subscribeToUserId, @AuthenticationPrincipal Long userId) {

        subscribeService.deleteBySubscriberIdAndSubscribedToId(subscribeToUserId, userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 특정 사용자의 구독 목록 조회
    @GetMapping("/{subscriberId}")
    public ResponseEntity<Page<UserSearchResponse>> getSubscribe(@PathVariable Long subscriberId,
                                                             @AuthenticationPrincipal Long userId,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "5") int size
    ) {
        Page<UserSearchResponse> response = subscribeService.findMySubscribedUsers(subscriberId, userId, PageRequest.of(page, size));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // 특정 사용자의 구독자 조회
    @GetMapping("/{subscribedId}/fans")
    public ResponseEntity<Page<UserSearchResponse>> getSubscriber(@PathVariable Long subscribedId,
                                                                  @AuthenticationPrincipal Long userId,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "5") int size
    ) {
        Page<UserSearchResponse> response = subscribeService.findMySubscriberUsers(subscribedId, userId, PageRequest.of(page, size));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
