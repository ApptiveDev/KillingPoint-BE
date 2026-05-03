package apptive.team5.booth.controller;

import apptive.team5.booth.dto.PublicKillingPartResponse;
import apptive.team5.booth.dto.PublicUserResponse;
import apptive.team5.booth.service.BoothService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class BoothController {

    private final BoothService boothService;

    @GetMapping("/users/search")
    public ResponseEntity<PublicUserResponse> findUserByTag(@RequestParam String tag) {
        PublicUserResponse response = boothService.findUserByTag(tag);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/users/{userId}/playlist")
    public ResponseEntity<Page<PublicKillingPartResponse>> getUserPlaylist(
            @PathVariable
            Long userId,
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "5")
            int size
    ) {
        Page<PublicKillingPartResponse> response =
                boothService.getUserPlaylist(userId, PageRequest.of(page, size));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
