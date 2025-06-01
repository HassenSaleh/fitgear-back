package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.Configurations.JwtConfig;
import com.ecommerce.fitgearback.dtos.login.JwtRequest;
import com.ecommerce.fitgearback.dtos.login.JwtResponse;
import com.ecommerce.fitgearback.mappers.AdminMapper;
import com.ecommerce.fitgearback.mappers.CustomerMapper;
import com.ecommerce.fitgearback.mappers.VendorMapper;
import com.ecommerce.fitgearback.models.Admin;
import com.ecommerce.fitgearback.models.Customer;
import com.ecommerce.fitgearback.models.JwtUser;
import com.ecommerce.fitgearback.models.Vendor;
import com.ecommerce.fitgearback.repositories.AdminRepository;
import com.ecommerce.fitgearback.repositories.CustomerRepository;
import com.ecommerce.fitgearback.repositories.VendorRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final CustomerRepository customerRepository;
    private final AdminRepository adminRepository;
    private final VendorRepository vendorRepository;
    private final CustomerMapper customerMapper;
    private final VendorMapper vendorMapper;
    private final AdminMapper adminMapper;
    private final JwtConfig config;
    private final JwtUserLoader jwtUserLoader;

    public JwtResponse login(@NotNull JwtRequest request, HttpServletResponse response) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        JwtUser user = findUserByEmail(request.getEmail());
        String role = determineRole(user);
        String accessToken = jwtService.generateAccessToken(user, role);
        String refreshToken = jwtService.generateRefreshToken(user, role);

        setRefreshTokenCookie(response, refreshToken);
        List<String> roles = List.of(role);
        String redirectUrl = getRedirectUrl(roles);
        return new JwtResponse(accessToken, refreshToken, roles, redirectUrl, null);
    }

    public JwtResponse refresh(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        var claims = jwtService.parseToken(refreshToken);
        if (claims == null || claims.get("userId") == null || claims.get("role") == null) {
            throw new BadCredentialsException("Invalid token claims");
        }

        Long userId;
        try {
            userId = Long.valueOf(claims.get("userId", String.class));
        } catch (NumberFormatException e) {
            throw new BadCredentialsException("Invalid user ID in token");
        }
        String role = claims.get("role", String.class);

        JwtUser user = jwtUserLoader.loadUserFromToken(refreshToken);
        String accessToken = jwtService.generateAccessToken(user, role);
        List<String> roles = List.of(role);
        String redirectUrl = getRedirectUrl(roles);
        return new JwtResponse(accessToken, null, roles, redirectUrl, null);
    }

    public Object getCurrentUserInfo() {
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();

        return switch (role) {
            case "CUSTOMER" -> customerRepository.findById(userId)
                    .map(customerMapper::toResponse)
                    .orElseThrow(() -> new UsernameNotFoundException("Customer not found with ID: " + userId));
            case "ADMIN" -> adminRepository.findById(userId)
                    .map(adminMapper::toResponse)
                    .orElseThrow(() -> new UsernameNotFoundException("Admin not found with ID: " + userId));
            case "VENDOR" -> vendorRepository.findById(userId)
                    .map(vendorMapper::toResponse)
                    .orElseThrow(() -> new UsernameNotFoundException("Vendor not found with ID: " + userId));
            default -> throw new UsernameNotFoundException("Invalid role: " + role);
        };
    }

    public Customer getCurrentCustomer() {
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();
        if (!"CUSTOMER".equals(role)) {
            throw new AccessDeniedException("User is not a customer");
        }
        return customerRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found with ID: " + userId));
    }

    public Vendor getCurrentVendor() {
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();
        if (!"VENDOR".equals(role)) {
            throw new AccessDeniedException("User is not a vendor");
        }
        return vendorRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Vendor not found with ID: " + userId));
    }

    public Admin getCurrentAdmin() {
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();
        if (!"ADMIN".equals(role)) {
            throw new AccessDeniedException("User is not an admin");
        }
        return adminRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found with ID: " + userId));
    }

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            throw new UsernameNotFoundException("Unauthorized or invalid user principal");
        }
        return userId;
    }

    private String getCurrentUserRole() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().isEmpty()) {
            throw new UsernameNotFoundException("No role found for user");
        }
        return auth.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElseThrow(() -> new UsernameNotFoundException("No role found for user"));
    }

    private JwtUser findUserByEmail(String email) {
        return adminRepository.findByEmail(email)
                .map(user -> (JwtUser) user)
                .orElseGet(() -> vendorRepository.findByEmail(email)
                        .map(user -> (JwtUser) user)
                        .orElseGet(() -> customerRepository.findByEmail(email)
                                .map(user -> (JwtUser) user)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + email))));
    }

    private String determineRole(JwtUser user) {
        if (user instanceof Admin) return "ADMIN";
        if (user instanceof Vendor) return "VENDOR";
        if (user instanceof Customer) return "CUSTOMER";
        throw new IllegalStateException("Unknown user type");
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        var cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/v1/auth/refresh");
        cookie.setMaxAge(config.getRefreshTokenExpiration());
        cookie.setSecure(config.isSecureCookies());
        response.addCookie(cookie);
    }

    private String getRedirectUrl(List<String> roles) {
        if (roles.contains("ADMIN")) return "/admin/dashboard";
        if (roles.contains("VENDOR")) return "/vendor/dashboard";
        if (roles.contains("CUSTOMER")) return "/customer/dashboard";
        return "/login";
    }
}