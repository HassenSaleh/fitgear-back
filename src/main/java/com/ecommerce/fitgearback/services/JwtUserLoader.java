package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.Configurations.JwtConfig;
import com.ecommerce.fitgearback.models.JwtUser;
import com.ecommerce.fitgearback.repositories.AdminRepository;
import com.ecommerce.fitgearback.repositories.CustomerRepository;
import com.ecommerce.fitgearback.repositories.VendorRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
@Getter
@Setter
@AllArgsConstructor
@Service
public class JwtUserLoader {

    private final JwtConfig config;
    private final CustomerRepository customerRepository;
    private final AdminRepository adminRepository;
    private final VendorRepository vendorRepository;

    public JwtUser loadUserFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(config.getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Long userId = Long.valueOf(claims.get("userId", String.class));
        String role = claims.get("role", String.class);
        return switch (role.toUpperCase()) {
            case "ADMIN" -> adminRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("Admin not found with ID: " + userId));
            case "VENDOR" -> vendorRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("Vendor not found with ID: " + userId));
            case "CUSTOMER" -> customerRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("Customer not found with ID: " + userId));
            default -> throw new UsernameNotFoundException("Invalid role: " + role);
        };
    }
}