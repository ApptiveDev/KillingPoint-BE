package apptive.team5.admin.controller;

import apptive.team5.alarmv2.domain.AlarmDispatchV2;
import apptive.team5.alarmv2.service.AlarmDispatchV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/dashboard/alarms-v2")
@RequiredArgsConstructor
public class AlarmDispatchV2AdminController {

    private final AlarmDispatchV2Service alarmDispatchV2Service;

    @PostMapping("/dispatch")
    public String dispatch(@RequestParam(defaultValue = "") String title,
                           @RequestParam(defaultValue = "") String content,
                           @RequestParam(defaultValue = "/") String deepLink,
                           @RequestParam(defaultValue = "all") String recipientMode,
                           @RequestParam(required = false) List<Long> selectedRecipientIds,
                           @RequestParam(defaultValue = "IMMEDIATE") String dispatchType,
                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime scheduledAt,
                           RedirectAttributes redirectAttributes) {
        try {
            AlarmDispatchV2 dispatch = alarmDispatchV2Service.createDispatch(
                    title, content, deepLink, recipientMode, selectedRecipientIds, dispatchType, scheduledAt);
            redirectAttributes.addFlashAttribute("alarmV2Success", dispatch.getDispatchType().name().equals("SCHEDULED")
                    ? "알림 예약을 등록했습니다."
                    : "알림 발송을 시작했습니다.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("alarmV2Error", exception.getMessage());
            redirectAttributes.addFlashAttribute("alarmV2Title", title);
            redirectAttributes.addFlashAttribute("alarmV2Content", content);
            redirectAttributes.addFlashAttribute("alarmV2DeepLink", deepLink);
            if (selectedRecipientIds != null) {
                selectedRecipientIds.forEach(id -> redirectAttributes.addAttribute("selectedRecipientIds", id));
            }
        }
        redirectAttributes.addAttribute("view", "alarms-v2");
        redirectAttributes.addAttribute("alarmV2Tab", "send");
        redirectAttributes.addAttribute("alarmV2RecipientMode", recipientMode);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/{dispatchId}/cancel")
    public String cancelSchedule(@PathVariable Long dispatchId, RedirectAttributes redirectAttributes) {
        try {
            alarmDispatchV2Service.cancelScheduledDispatch(dispatchId);
            redirectAttributes.addFlashAttribute("alarmV2Success", "예약 알림을 취소했습니다.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("alarmV2Error", exception.getMessage());
        }
        redirectAttributes.addAttribute("view", "alarms-v2");
        redirectAttributes.addAttribute("alarmV2Tab", "schedules");
        return "redirect:/admin/dashboard";
    }
}
