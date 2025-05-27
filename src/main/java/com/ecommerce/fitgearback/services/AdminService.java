package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.dtos.AdminDto;
import com.ecommerce.fitgearback.dtos.AdminResponseDto;
import com.ecommerce.fitgearback.mappers.AdminMapper;
import com.ecommerce.fitgearback.mappers.UserMapper;
import com.ecommerce.fitgearback.models.Admin;

import com.ecommerce.fitgearback.models.Role;
import com.ecommerce.fitgearback.repositories.AdminRepository;
import com.ecommerce.fitgearback.repositories.RoleRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@Service
public class AdminService {
    private final AdminRepository adminRepository;
    private final UserMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public AdminResponseDto registerAdmin(AdminDto dto){
        Admin admin = adminMapper.toAdmin(dto);
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        Role role = roleRepository
                .findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        admin.setRoles(Set.of(role));
        Admin saved = adminRepository.save(admin);
        return adminMapper.toAdminResponseDTO(saved);
    }
}
