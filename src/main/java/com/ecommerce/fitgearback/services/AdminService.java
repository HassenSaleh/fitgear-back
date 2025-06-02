package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.dtos.admin.AdminRequest;
import com.ecommerce.fitgearback.dtos.admin.AdminResponse;
import com.ecommerce.fitgearback.exceptions.ResourceNotFoundException;
import com.ecommerce.fitgearback.mappers.AdminMapper;
import com.ecommerce.fitgearback.models.Admin;
import com.ecommerce.fitgearback.models.Role;
import com.ecommerce.fitgearback.repositories.AdminRepository;
import com.ecommerce.fitgearback.repositories.RoleRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@Service
public class AdminService {
    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public AdminResponse sign(AdminRequest dto){
        Admin admin = adminMapper.toAdmin(dto);
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        Role role = roleRepository
                .findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        admin.setRoles(Set.of(role));
        Admin saved = adminRepository.save(admin);
        return adminMapper.toResponse(saved);
    }



    public AdminResponse updateAdmin(Long id, @NotNull AdminRequest dto) {
        Admin existing = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + id));
        existing.setFirstName(dto.getFirstName());
        existing.setLastName(dto.getLastName());
        existing.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        Admin updated = adminRepository.save(existing);
        return adminMapper.toResponse(updated);
    }

    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }

    public AdminResponse getAdminById(Long id) {
        Admin admin = adminRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + id));
        return adminMapper.toResponse(admin);
    }

    public List<AdminResponse> getAllAdmins() {
        return adminRepository.findAll().stream()
                .map(adminMapper::toResponse)
                .toList();
    }

    public AdminResponse getAdminByUsername(String email) throws UsernameNotFoundException {
        var admin = adminRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("Admin not found with email: " + email));
        if (admin == null) {
            return null;
        }
        return adminMapper.toResponse(admin);

    }
}
