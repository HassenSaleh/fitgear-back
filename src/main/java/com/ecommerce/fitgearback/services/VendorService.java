package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.dtos.VendorDto;
import com.ecommerce.fitgearback.dtos.VendorResponseDto;
import com.ecommerce.fitgearback.exceptions.ResourceNotFoundException;
import com.ecommerce.fitgearback.mappers.UserMapper;
import com.ecommerce.fitgearback.models.Role;
import com.ecommerce.fitgearback.models.Vendor;
import com.ecommerce.fitgearback.repositories.RoleRepository;
import com.ecommerce.fitgearback.repositories.UserRepository;
import com.ecommerce.fitgearback.repositories.VendorRepository;
import jakarta.transaction.Transactional;
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
import java.util.stream.Collectors;

@Setter
@Getter
@AllArgsConstructor
@Service
public class VendorService {
    private final VendorRepository vendorRepository;
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

    public List<VendorResponseDto> getAllVendors() {
        return vendorRepository.findAll()
                .stream()
                .map(vendorMapper::toVendorResponseDTO)
                .collect(Collectors.toList());
    }

    public VendorResponseDto getVendorById(Long id) {
        Vendor vendor = vendorRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));
        return vendorMapper.toVendorResponseDTO(vendor);
    }


    public VendorResponseDto updateVendor(Long id, VendorDto dto) {
        Vendor vendor = vendorRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));

        vendor.setFirstName(dto.getFirstName());
        vendor.setLastName(dto.getLastName());
        vendor.setEmail(dto.getEmail());

        Vendor updatedVendor = vendorRepository.save(vendor);
        return vendorMapper.toVendorResponseDTO(updatedVendor);
    }

    public void deleteVendor(Long id) {
        if (!vendorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vendor not found with id: " + id);
        }
        vendorRepository.deleteById(id);
    }

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var vendor = vendorRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("User not found with email: " + email));
        return new User(
                vendor.getEmail(),
                vendor.getPassword(),
                Collections.emptyList()
        );
    }



//    public LoginResponseDto login(LoginRequestDto loginRequest) {
//        Vendor vendor = vendorRepository
//                .findByEmail(loginRequest.getEmail())
//                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with email: " + loginRequest.getEmail()));
//
//        if (passwordEncoder.matches(loginRequest.getPassword(), vendor.getPassword())) {
//            return new LoginResponseDto(
//                    vendor.getId(),
//                    vendor.getEmail(),
//                    "VENDOR",
//                    "Login successful"
//            );
//        } else {
//            throw new RuntimeException("Invalid credentials");
//        }
//    }

}
