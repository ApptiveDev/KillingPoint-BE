package apptive.team5.admin.controller;

import apptive.team5.alarmv2.service.AdminNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/dashboard/alarms-v2/notices")
@RequiredArgsConstructor
@Slf4j
public class AdminNoticeController {

    private final AdminNoticeService adminNoticeService;

    @PostMapping
    public String publish(@RequestParam(defaultValue = "") String title,
                          @RequestParam(defaultValue = "") String content,
                          RedirectAttributes redirectAttributes) {
        try {
            adminNoticeService.publish(title, content);
            redirectAttributes.addFlashAttribute("alarmV2Success", "공지를 등록했습니다.");
        } catch (IllegalArgumentException exception) {
            preserveNoticeInput(redirectAttributes, title, content);
            redirectAttributes.addFlashAttribute("alarmV2Error", exception.getMessage());
        } catch (RuntimeException exception) {
            log.error("Failed to register admin notice", exception);
            preserveNoticeInput(redirectAttributes, title, content);
            redirectAttributes.addFlashAttribute("alarmV2Error", "공지 등록 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
        redirectAttributes.addAttribute("view", "alarms-v2");
        redirectAttributes.addAttribute("alarmV2Tab", "notices");
        return "redirect:/admin/dashboard";
    }

    private void preserveNoticeInput(RedirectAttributes redirectAttributes, String title, String content) {
        redirectAttributes.addFlashAttribute("alarmV2NoticeTitle", title == null ? "" : title);
        redirectAttributes.addFlashAttribute("alarmV2NoticeContent", content == null ? "" : content);
    }
}
