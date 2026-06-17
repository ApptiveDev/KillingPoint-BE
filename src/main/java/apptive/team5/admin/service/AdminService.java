package apptive.team5.admin.service;

import apptive.team5.admin.dto.AdminLoginRequest;
import apptive.team5.admin.entity.Admin;
import apptive.team5.admin.exception.AdminException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static apptive.team5.admin.exception.AdminErrorCode.INVALID_ADMIN_PASSWORD;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final AdminLowService adminLowService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Admin authenticateAdmin(AdminLoginRequest request) {

        Admin adminMember = adminLowService.findByAdminId(request.getAdminId());

        if (!passwordEncoder.matches(request.getPassword(), adminMember.getPassword())) {
            throw new AdminException(INVALID_ADMIN_PASSWORD);
        }

        return adminMember;
    }
}
