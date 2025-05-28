package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.dtos.AdminDto;
import com.ecommerce.fitgearback.dtos.AdminResponseDto;
import com.ecommerce.fitgearback.exceptions.ResourceNotFoundException;
import com.ecommerce.fitgearback.mappers.UserMapper;
import com.ecommerce.fitgearback.models.Admin;

import com.ecommerce.fitgearback.models.Role;
import com.ecommerce.fitgearback.repositories.AdminRepository;
import com.ecommerce.fitgearback.repositories.RoleRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
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

    public AdminResponseDto updateAdmin(Long id, AdminDto dto) {
        Admin existing = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + id));
        existing.setFirstName(dto.getFirstName());
        existing.setLastName(dto.getLastName());
        existing.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        Admin updated = adminRepository.save(existing);
        return adminMapper.toAdminResponseDTO(updated);
    }

    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }

    public AdminResponseDto getAdminById(Long id) {
        Admin admin = adminRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + id));
        return adminMapper.toAdminResponseDTO(admin);
    }

    public List<AdminResponseDto> getAllAdmins() {
        return adminRepository.findAll().stream()
                .map(adminMapper::toAdminResponseDTO)
                .toList();
    }

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var admin = adminRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("Admin not found with email: " + email));
        return new User(
                admin.getEmail(),
                admin.getPassword(),
                Collections.emptyList()
        );
    }

//
//    public LoginResponseDto login(LoginRequestDto loginRequest) {
//        Admin admin = adminRepository
//                .findByEmail(loginRequest.getEmail())
//                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with email: " + loginRequest.getEmail()));
//
//        if (passwordEncoder.matches(loginRequest.getPassword(), admin.getPassword())) {
//            return new LoginResponseDto(
//                    admin.getId(),
//                    admin.getEmail(),
//                    "ADMIN",
//                    "Login successful"
//            );
//        } else {
//            throw new RuntimeException("Invalid credentials");
//        }
//    }

}
