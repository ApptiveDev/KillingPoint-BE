package apptive.team5.user.controller;

import apptive.team5.file.dto.FileUploadRequest;
import apptive.team5.user.dto.InitSettingsRequest;
import apptive.team5.user.dto.InitSettingsResponse;
import apptive.team5.user.dto.InitSettingsResponse.AppUpdateStatus;
import apptive.team5.user.dto.PolicyAgreementRequest;
import apptive.team5.user.dto.PolicyStatusResponse;
import apptive.team5.user.dto.UserResponse;
import apptive.team5.user.dto.UserSearchResponse;
import apptive.team5.user.dto.UserStaticsResponse;
import apptive.team5.user.dto.UserTagUpdateRequest;
import apptive.team5.user.service.UserInitSettingService;
import apptive.team5.user.service.UserPolicyService;
import apptive.team5.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
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
public class UserController {

    private final UserService userService;
    private final UserPolicyService userPolicyService;
    private final UserInitSettingService userInitSettingService;

    @GetMapping("/my")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal Long userId) {

        UserResponse response = userService.getUserInfo(userId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/my")
    public ResponseEntity<Void> wirthDrawUser(@AuthenticationPrincipal Long userId) {

        userService.deleteUser(userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/my/tags")
    public ResponseEntity<UserResponse> changeUserTag(@Valid @RequestBody UserTagUpdateRequest userTagUpdateRequest,
                                                @AuthenticationPrincipal Long userId) {

        UserResponse response = userService.changeTag(userTagUpdateRequest, userId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/my/profile-image")
    public ResponseEntity<UserResponse> changeProfileImage(@Valid @RequestBody FileUploadRequest fileUploadRequest,
                                                           @AuthenticationPrincipal Long userId) {
        UserResponse userResponse = userService.changeProfileImage(fileUploadRequest, userId);

        return  ResponseEntity.status(HttpStatus.OK).body(userResponse);
    }

    // 기본 이미지로 변경
    @DeleteMapping("/my/profile-image")
    public ResponseEntity<UserResponse> deleteProfileImage(@AuthenticationPrincipal Long userId) {
        UserResponse userResponse = userService.deleteProfileImage(userId);

        return ResponseEntity.status(HttpStatus.OK).body(userResponse);
    }


    @GetMapping
    public ResponseEntity<Page<UserSearchResponse>> getUserList(@RequestParam(required = false) String searchCond,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "5") int size,
                                                                @AuthenticationPrincipal Long userId) {
        Page<UserSearchResponse> response = userService.findByTagOrUserName(userId, searchCond, PageRequest.of(page, size));


        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @GetMapping("/{userId}/statics")
    public ResponseEntity<UserStaticsResponse> getUserStatics(@PathVariable Long userId) {
        UserStaticsResponse response = userService.getUserStatics(userId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/init-settings")
    public ResponseEntity<InitSettingsResponse> getInitSettings(@AuthenticationPrincipal Long userId,
                                                                @Valid @ModelAttribute InitSettingsRequest request) {

        AppUpdateStatus appUpdateStatus = userInitSettingService.checkAppUpdate(request.clientType(), request.clientVersion());
        List<PolicyStatusResponse> policies = userPolicyService.getPolicyStatuses(userId);
        boolean needsPolicyAgreement = policies.stream().anyMatch(PolicyStatusResponse::needsUpdate);
        boolean needsTagSetup = userInitSettingService.checkNeedsTagSetup(userId);

        return ResponseEntity.ok(new InitSettingsResponse(appUpdateStatus, needsPolicyAgreement, needsTagSetup, policies));
    }

    @PostMapping("/policy-agreement")
    public ResponseEntity<Void> agreePolicy(@Valid @RequestBody PolicyAgreementRequest request,
                                            @AuthenticationPrincipal Long userId) {

        userPolicyService.agreePolicy(userId, request);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
