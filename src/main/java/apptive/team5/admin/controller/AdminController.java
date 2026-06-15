package apptive.team5.admin.controller;

import apptive.team5.admin.dto.AdminLoginRequest;
import apptive.team5.admin.dto.AdminUgcSearchType;
import apptive.team5.admin.entity.Admin;
import apptive.team5.admin.exception.AdminException;
import apptive.team5.admin.service.AdminService;
import apptive.team5.admin.service.AdminUgcService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static apptive.team5.admin.util.SessionConst.ADMIN_SESSION_KEY;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AdminUgcService adminUgcService;

    @GetMapping
    public String adminHome(HttpServletRequest request) {
        if (isLoggedIn(request)) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/admin/login";
    }

    @GetMapping("/login")
    public String loginForm(HttpServletRequest request, Model model) {
        if (isLoggedIn(request)) {
            return "redirect:/admin/dashboard";
        }

        if (!model.containsAttribute("adminLoginRequest")) {
            model.addAttribute("adminLoginRequest", new AdminLoginRequest());
        }
        return "admin/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute AdminLoginRequest adminLoginRequest,
                        BindingResult bindingResult,
                        HttpServletRequest request,
                        Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/login";
        }

        try {
            Admin admin = adminService.authenticateAdmin(adminLoginRequest);
            renewAdminSession(request, admin);
            return "redirect:/admin/dashboard";
        } catch (AdminException e) {
            model.addAttribute("loginError", e.getMessage());
            adminLoginRequest.setPassword(null);
            return "admin/login";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size,
                            @RequestParam(defaultValue = "all") String filter,
                            @RequestParam(defaultValue = "MUSIC_TITLE") String searchType,
                            @RequestParam(defaultValue = "") String q,
                            Model model) {
        int pageNumber = Math.max(page, 0);
        int pageSize = Math.min(Math.max(size, 10), 50);
        String ugcFilter = "reported".equals(filter) ? "reported" : "all";
        AdminUgcSearchType ugcSearchType = AdminUgcSearchType.from(searchType);
        String query = q == null ? "" : q.trim();
        var ugcPage = adminUgcService.getPublicUgcItems(
                ugcFilter,
                ugcSearchType,
                query,
                PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createDateTime"))
        );
        var ugcItems = ugcPage.getContent();

        model.addAttribute("ugcItems", ugcItems);
        model.addAttribute("selectedUgc", ugcItems.isEmpty() ? null : ugcItems.getFirst());
        model.addAttribute("ugcPage", ugcPage);
        model.addAttribute("ugcFilter", ugcFilter);
        model.addAttribute("ugcSearchTypes", AdminUgcSearchType.values());
        model.addAttribute("ugcSearchType", ugcSearchType);
        model.addAttribute("ugcQuery", query);
        model.addAttribute("pageSize", pageSize);
        return "admin/dashboard";
    }

    @PostMapping("/dashboard/{diaryId}/memo")
    public String saveMemo(@PathVariable Long diaryId,
                           @RequestParam(defaultValue = "") String memo,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size,
                           @RequestParam(defaultValue = "all") String filter,
                           @RequestParam(defaultValue = "MUSIC_TITLE") String searchType,
                           @RequestParam(defaultValue = "") String q,
                           RedirectAttributes redirectAttributes) {
        adminUgcService.saveMemo(diaryId, memo);

        redirectAttributes.addAttribute("page", Math.max(page, 0));
        redirectAttributes.addAttribute("size", Math.min(Math.max(size, 10), 50));
        redirectAttributes.addAttribute("filter", "reported".equals(filter) ? "reported" : "all");
        redirectAttributes.addAttribute("searchType", AdminUgcSearchType.from(searchType).name());
        redirectAttributes.addAttribute("q", q == null ? "" : q.trim());
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/dashboard/{diaryId}/delete")
    public String deleteDiary(@PathVariable Long diaryId,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size,
                              @RequestParam(defaultValue = "all") String filter,
                              @RequestParam(defaultValue = "MUSIC_TITLE") String searchType,
                              @RequestParam(defaultValue = "") String q,
                              RedirectAttributes redirectAttributes) {
        adminUgcService.deleteDiary(diaryId);

        redirectAttributes.addAttribute("page", Math.max(page, 0));
        redirectAttributes.addAttribute("size", Math.min(Math.max(size, 10), 50));
        redirectAttributes.addAttribute("filter", "reported".equals(filter) ? "reported" : "all");
        redirectAttributes.addAttribute("searchType", AdminUgcSearchType.from(searchType).name());
        redirectAttributes.addAttribute("q", q == null ? "" : q.trim());
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/admin/login";
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(ADMIN_SESSION_KEY) != null;
    }

    private void renewAdminSession(HttpServletRequest request, Admin admin) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        request.getSession(true).setAttribute(ADMIN_SESSION_KEY, admin);
    }
}
