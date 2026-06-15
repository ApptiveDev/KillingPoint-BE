package apptive.team5.admin.service;

import apptive.team5.admin.entity.Admin;
import apptive.team5.admin.repository.AdminRepository;
import apptive.team5.global.exception.NotFoundEntityException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminLowService {

    private final AdminRepository adminRepository;

    Admin findByAdminId(String adminId) {
        return  adminRepository
                .findByAdminId(adminId)
                .orElseThrow(() -> new NotFoundEntityException("존재하지 않는 관리자 ID입니다."));
    }
}
