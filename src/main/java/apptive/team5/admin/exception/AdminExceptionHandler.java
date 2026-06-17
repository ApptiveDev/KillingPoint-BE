package apptive.team5.admin.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackages = "apptive.team5.admin")
public class AdminExceptionHandler {

    @ExceptionHandler(AdminException.class)
    public ModelAndView handleAdminException(AdminException e) {
        AdminErrorCode errorCode = e.getErrorCode();

        ModelAndView modelAndView = new ModelAndView("admin/error");
        modelAndView.setStatus(errorCode.getHttpStatus());
        modelAndView.addObject("status", errorCode.getHttpStatus().value());
        modelAndView.addObject("message", errorCode.getMessage());
        return modelAndView;
    }
}
