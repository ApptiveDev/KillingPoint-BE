package apptive.team5.admin.service;

import apptive.team5.admin.dto.AdminLoginRequest;
import apptive.team5.admin.entity.Admin;
import apptive.team5.global.exception.NotFoundEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final AdminLowService adminLowService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Admin authenticateAdmin(AdminLoginRequest request) {

        Admin adminMember = adminLowService.findByAdminId(request.getAdminId());

        if (!passwordEncoder.matches(request.getPassword(), adminMember.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return adminMember;
    }
}
