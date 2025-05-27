package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.dtos.VendorDto;
import com.ecommerce.fitgearback.dtos.VendorResponseDto;
import com.ecommerce.fitgearback.mappers.UserMapper;
import com.ecommerce.fitgearback.mappers.VendorMapper;
import com.ecommerce.fitgearback.models.Role;
import com.ecommerce.fitgearback.models.Vendor;
import com.ecommerce.fitgearback.repositories.RoleRepository;
import com.ecommerce.fitgearback.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Setter
@Getter
@AllArgsConstructor
@Service
public class VendorService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper vendorMapper;

    @Transactional
    public VendorResponseDto registerVendor(VendorDto vendorDto) {
        Vendor vendor = vendorMapper.toVendor(vendorDto);
        vendor.setPassword(passwordEncoder.encode(vendorDto.getPassword()));
        Role vendorRole = roleRepository
                .findByName("VENDOR")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        vendor.setRoles(Collections.singleton(vendorRole));
        Vendor savedVendor = userRepository.save(vendor);
        return vendorMapper.toVendorResponseDTO(savedVendor);
    }
}
