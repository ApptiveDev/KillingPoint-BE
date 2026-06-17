package apptive.team5.admin.service;

import apptive.team5.admin.entity.Admin;
import apptive.team5.admin.exception.AdminException;
import apptive.team5.admin.repository.AdminRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static apptive.team5.admin.exception.AdminErrorCode.NOT_FOUND_ADMIN;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminLowService {

    private final AdminRepository adminRepository;

    Admin findByAdminId(String adminId) {
        return  adminRepository
                .findByAdminId(adminId)
                .orElseThrow(() -> new AdminException(NOT_FOUND_ADMIN));
    }
}
