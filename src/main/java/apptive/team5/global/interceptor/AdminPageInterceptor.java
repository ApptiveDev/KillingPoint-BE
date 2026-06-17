package apptive.team5.global.interceptor;

import apptive.team5.admin.entity.Admin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static apptive.team5.admin.util.SessionConst.ADMIN_SESSION_KEY;

@Component
public class AdminPageInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null) {
            redirectToLogin(response);
            return false;
        }

        Admin adminMember = (Admin) session.getAttribute(ADMIN_SESSION_KEY);
        if (adminMember == null) {
            redirectToLogin(response);
            return false;
        }

        return true;
    }

    private void redirectToLogin(HttpServletResponse response) throws Exception {
        response.sendRedirect("/admin/login");
    }
}

