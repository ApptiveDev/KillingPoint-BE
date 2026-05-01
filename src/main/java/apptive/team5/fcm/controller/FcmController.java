package apptive.team5.fcm.controller;

import apptive.team5.fcm.dto.DeviceTokenRequest;
import apptive.team5.fcm.service.FcmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("/tokens")
    public ResponseEntity<Void> addToken(@Valid @RequestBody DeviceTokenRequest deviceTokenRequest,
                                         @AuthenticationPrincipal Long userId){
        fcmService.addDeviceToken(userId, deviceTokenRequest);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/tokens")
    public ResponseEntity<Void> deleteToken(@AuthenticationPrincipal Long userId){

        fcmService.deleteDeviceTokenByUserId(userId);

        return  ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
