package apptive.team5.alarmv2.controller;

import apptive.team5.alarmv2.dto.NoticeDetailResponse;
import apptive.team5.alarmv2.dto.NoticeSummaryResponse;
import apptive.team5.alarmv2.service.AdminNoticeService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
@Validated
public class NoticeApiController {

    private final AdminNoticeService adminNoticeService;

    @GetMapping
    public ResponseEntity<Page<NoticeSummaryResponse>> getNotices(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        Page<NoticeSummaryResponse> notices = adminNoticeService.getNotices(PageRequest.of(
                page, size, Sort.by(Sort.Order.desc("createDateTime"), Sort.Order.desc("id"))));
        return ResponseEntity.ok(notices);
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeDetailResponse> getNotice(@PathVariable @Min(1) Long noticeId) {
        return ResponseEntity.ok(adminNoticeService.getNotice(noticeId));
    }
}
