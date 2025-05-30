package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.Configurations.JwtConfig;
import com.ecommerce.fitgearback.dtos.login.JwtResponse;
import com.ecommerce.fitgearback.dtos.login.LoginRequest;
import com.ecommerce.fitgearback.dtos.login.LoginResponse;
import com.ecommerce.fitgearback.mappers.AdminMapper;
import com.ecommerce.fitgearback.mappers.CustomerMapper;
import com.ecommerce.fitgearback.mappers.VendorMapper;
import com.ecommerce.fitgearback.models.*;
import com.ecommerce.fitgearback.repositories.AdminRepository;
import com.ecommerce.fitgearback.repositories.CustomerRepository;
import com.ecommerce.fitgearback.repositories.UserRepository;
import com.ecommerce.fitgearback.repositories.VendorRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class AuthService {
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AdminRepository adminRepository;
    private final VendorRepository vendorRepository;
    private final CustomerMapper customerMapper;
    private final VendorMapper vendorMapper;
    private final AdminMapper adminMapper;
    private final JwtConfig config;

    public ResponseEntity<JwtResponse> login(LoginRequest request, HttpServletResponse response) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        var cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/v1/auth/refresh");
        cookie.setMaxAge(config.getRefreshTokenExpiration());
        cookie.setSecure(false);
        response.addCookie(cookie);

        var roles = user.getRoles().stream().map(Role::getName).toList();
        var redirectUrl = getRedirectUrl(roles);

        return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken, roles, redirectUrl));
    }

    public ResponseEntity<JwtResponse> refresh(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var userId = jwtService.getUserIdFromToken(refreshToken);
        var user = userRepository.findById(userId).orElseThrow();

        var accessToken = jwtService.generateAccessToken(user);
        var roles = user.getRoles().stream().map(Role::getName).toList();
        var redirectUrl = getRedirectUrl(roles);

        return ResponseEntity.ok(new JwtResponse(accessToken, null, roles, redirectUrl));
    }

    public ResponseEntity<?> getCurrentUserInfo() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var userId = (Long) auth.getPrincipal();

        return userRepository.findById(userId)
                .map(user -> {
                    if (hasRole(user, "CUSTOMER")) {
                        var customer = customerRepository.findById(userId).orElse(null);
                        return ResponseEntity.ok(customerMapper.toResponse(customer));
                    } else if (hasRole(user, "ADMIN")) {
                        var admin = adminRepository.findById(userId).orElse(null);
                        return ResponseEntity.ok(adminMapper.toResponse(admin));
                    } else if (hasRole(user, "VENDOR")) {
                        var vendor = vendorRepository.findById(userId).orElse(null);
                        return ResponseEntity.ok(vendorMapper.toResponse(vendor));
                    }
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                })
                .orElse(ResponseEntity.notFound().build());
    }


    public User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            throw new RuntimeException("Unauthorized or invalid user principal");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Customer getCurrentCustomer() {
        User user = getCurrentUser();
        if (!hasRole(user, "CUSTOMER")) {
            throw new RuntimeException("Current user is not a customer");
        }
        return customerRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public Admin getCurrentAdmin() {
        User user = getCurrentUser();
        if (!hasRole(user, "ADMIN")) {
            throw new RuntimeException("Current user is not an admin");
        }
        return adminRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));
    }

    public Vendor getCurrentVendor() {
        User user = getCurrentUser();
        if (!hasRole(user, "VENDOR")) {
            throw new RuntimeException("Current user is not a vendor");
        }
        return vendorRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
    }

    private @NotNull String getRedirectUrl(@NotNull List<String> roles) {
        if (roles.contains("ADMIN")) return "/admin/dashboard";
        if (roles.contains("VENDOR")) return "/vendor/dashboard";
        if (roles.contains("CUSTOMER")) return "/customer/dashboard";
        return "/login";
    }

    private boolean hasRole(@NotNull User user, String roleName) {
        return user.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(roleName));
    }
}
