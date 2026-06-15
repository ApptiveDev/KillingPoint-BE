package apptive.team5.admin.controller;

import apptive.team5.admin.dto.AdminLoginRequest;
import apptive.team5.admin.entity.Admin;
import apptive.team5.admin.exception.AdminException;
import apptive.team5.admin.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static apptive.team5.admin.util.SessionConst.ADMIN_SESSION_KEY;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

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
    public String dashboard() {
        return "admin/dashboard";
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
