package apptive.team5.admin.controller;

import apptive.team5.admin.dto.AdminLoginRequest;
import apptive.team5.admin.dto.AdminUgcSearchType;
import apptive.team5.admin.dto.UserSearchType;
import apptive.team5.admin.entity.Admin;
import apptive.team5.admin.exception.AdminException;
import apptive.team5.admin.service.AdminService;
import apptive.team5.admin.service.AdminUgcService;
import apptive.team5.admin.service.UserManagementService;
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
    private final UserManagementService userManagementService;

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
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(defaultValue = "ugc") String view,
                            @RequestParam(defaultValue = "all") String filter,
                            @RequestParam(defaultValue = "MUSIC_TITLE") String searchType,
                            @RequestParam(defaultValue = "") String q,
                            @RequestParam(defaultValue = "USER_ID") String userSearchType,
                            @RequestParam(defaultValue = "") String userQ,
                            @RequestParam(defaultValue = "") String selectedUserId,
                            @RequestParam(defaultValue = "0") int kpPage,
                            @RequestParam(defaultValue = "all") String kpFilter,
                            Model model) {
        int pageNumber = Math.max(page, 0);
        int pageSize = Math.min(Math.max(size, 10), 50);
        String adminView = resolveAdminView(view);
        int csPageSize = 10;

        model.addAttribute("adminView", adminView);
        model.addAttribute("pageTitle", pageTitle(adminView));

        if ("cs".equals(adminView)) {
            var memoPage = adminUgcService.getMemoItems(
                    PageRequest.of(pageNumber, csPageSize, Sort.by(Sort.Direction.DESC, "id"))
            );

            model.addAttribute("memoItems", memoPage.getContent());
            model.addAttribute("memoPage", memoPage);
            model.addAttribute("pageSize", csPageSize);
            return "admin/dashboard";
        }

        if ("users".equals(adminView)) {
            UserSearchType searchTypeForUser = UserSearchType.from(userSearchType);
            String query = userQ == null ? "" : userQ.trim();
            Long selectedUserIdValue = parseLongOrNull(selectedUserId);
            String selectedUserDiaryFilter = "reported".equals(kpFilter) ? "reported" : "all";
            var userPage = userManagementService.getUsers(
                    searchTypeForUser,
                    query,
                    PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "id"))
            );
            if (selectedUserIdValue != null) {
                model.addAttribute("selectedUser", userManagementService.getUser(selectedUserIdValue));
                var userDiaryPage = userManagementService.getUserDiaries(
                        selectedUserIdValue,
                        "reported".equals(selectedUserDiaryFilter),
                        PageRequest.of(Math.max(kpPage, 0), 5, Sort.by(Sort.Direction.DESC, "createDateTime"))
                );
                model.addAttribute("selectedUserDiaryItems", userDiaryPage.getContent());
                model.addAttribute("selectedUserDiaryPage", userDiaryPage);
            }

            model.addAttribute("userItems", userPage.getContent());
            model.addAttribute("userPage", userPage);
            model.addAttribute("userSearchTypes", UserSearchType.values());
            model.addAttribute("userSearchType", searchTypeForUser);
            model.addAttribute("userQuery", query);
            model.addAttribute("selectedUserId", selectedUserIdValue);
            model.addAttribute("selectedUserDiaryFilter", selectedUserDiaryFilter);
            model.addAttribute("pageSize", pageSize);
            return "admin/dashboard";
        }

        model.addAttribute("pageSize", pageSize);
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
        return "admin/dashboard";
    }

    @PostMapping("/dashboard/{diaryId}/memo")
    public String saveMemo(@PathVariable Long diaryId,
                           @RequestParam(defaultValue = "") String memo,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(defaultValue = "all") String filter,
                           @RequestParam(defaultValue = "MUSIC_TITLE") String searchType,
                           @RequestParam(defaultValue = "") String q,
                           RedirectAttributes redirectAttributes) {
        adminUgcService.saveMemo(diaryId, memo);

        redirectAttributes.addAttribute("page", Math.max(page, 0));
        redirectAttributes.addAttribute("size", Math.min(Math.max(size, 10), 50));
        redirectAttributes.addAttribute("view", "ugc");
        redirectAttributes.addAttribute("filter", "reported".equals(filter) ? "reported" : "all");
        redirectAttributes.addAttribute("searchType", AdminUgcSearchType.from(searchType).name());
        redirectAttributes.addAttribute("q", q == null ? "" : q.trim());
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/dashboard/memos/{memoId}/delete")
    public String deleteMemo(@PathVariable Long memoId,
                             @RequestParam(defaultValue = "0") int page,
                             RedirectAttributes redirectAttributes) {
        int pageSize = 10;
        long remainingCount = adminUgcService.deleteMemo(memoId);
        int lastPage = remainingCount == 0 ? 0 : (int) ((remainingCount - 1) / pageSize);

        redirectAttributes.addAttribute("view", "cs");
        redirectAttributes.addAttribute("page", Math.min(Math.max(page, 0), lastPage));
        redirectAttributes.addAttribute("size", pageSize);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/dashboard/users/{userId}/lock")
    public String changeUserLocked(@PathVariable Long userId,
                                   @RequestParam(defaultValue = "true") boolean locked,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(defaultValue = "USER_ID") String userSearchType,
                                   @RequestParam(defaultValue = "") String userQ,
                                   @RequestParam(defaultValue = "all") String kpFilter,
                                   @RequestParam(defaultValue = "0") int kpPage,
                                   RedirectAttributes redirectAttributes) {
        userManagementService.changeUserLocked(userId, locked);

        redirectAttributes.addAttribute("view", "users");
        redirectAttributes.addAttribute("page", Math.max(page, 0));
        redirectAttributes.addAttribute("size", Math.min(Math.max(size, 10), 50));
        redirectAttributes.addAttribute("userSearchType", UserSearchType.from(userSearchType).name());
        redirectAttributes.addAttribute("userQ", userQ == null ? "" : userQ.trim());
        redirectAttributes.addAttribute("selectedUserId", userId);
        redirectAttributes.addAttribute("kpFilter", "reported".equals(kpFilter) ? "reported" : "all");
        redirectAttributes.addAttribute("kpPage", Math.max(kpPage, 0));
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/dashboard/{diaryId}/delete")
    public String deleteDiary(@PathVariable Long diaryId,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "all") String filter,
                              @RequestParam(defaultValue = "MUSIC_TITLE") String searchType,
                              @RequestParam(defaultValue = "") String q,
                              RedirectAttributes redirectAttributes) {
        adminUgcService.deleteDiary(diaryId);

        redirectAttributes.addAttribute("page", Math.max(page, 0));
        redirectAttributes.addAttribute("size", Math.min(Math.max(size, 10), 50));
        redirectAttributes.addAttribute("view", "ugc");
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

    private String resolveAdminView(String view) {
        return switch (view) {
            case "cs" -> "cs";
            case "users" -> "users";
            default -> "ugc";
        };
    }

    private String pageTitle(String adminView) {
        return switch (adminView) {
            case "cs" -> "메모 기록";
            case "users" -> "유저";
            default -> "UGC 검수";
        };
    }

    private Long parseLongOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
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
