package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.login.JwtRequest;
import com.ecommerce.fitgearback.dtos.login.JwtResponse;
import com.ecommerce.fitgearback.dtos.login.Response;
import com.ecommerce.fitgearback.mappers.AdminMapper;
import com.ecommerce.fitgearback.mappers.CustomerMapper;
import com.ecommerce.fitgearback.mappers.VendorMapper;
import com.ecommerce.fitgearback.repositories.AdminRepository;
import com.ecommerce.fitgearback.repositories.CustomerRepository;
import com.ecommerce.fitgearback.repositories.VendorRepository;
import com.ecommerce.fitgearback.services.AuthService;
import com.ecommerce.fitgearback.services.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173" , allowCredentials = "true")
@Setter
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/auths")

public class AuthController {
    private final AuthService authService;
    private final CustomerMapper customerMapper;
    private final CustomerRepository customerRepository;
    private final AdminMapper adminMapper;
    private final AdminRepository adminRepository;
    private final VendorMapper vendorMapper;
    private final VendorRepository vendorRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authentication;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody JwtRequest request,
            HttpServletResponse response) {
        var jwtResponse = authService.login(request, response);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@CookieValue("refreshToken") String refreshToken) {
        var jwtResponse = authService.refresh(refreshToken);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/validate")
    public boolean validate(@RequestHeader("Authorization") String token) {

        return jwtService.validateToken(token);

    }

    @GetMapping("/test")
    public ResponseEntity<Object> test() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String role = auth.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElse(null);

        if (role == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return switch (role) {
            case "CUSTOMER" -> customerRepository.findById(userId)
                    .map(customerMapper::toResponse)
                    .map(dto -> ResponseEntity.ok((Object) dto))
                    .orElseGet(() -> ResponseEntity.notFound().build());
            case "ADMIN" -> adminRepository.findById(userId)
                    .map(adminMapper::toResponse)
                    .map(dto -> ResponseEntity.ok((Object) dto))
                    .orElseGet(() -> ResponseEntity.notFound().build());
            case "VENDOR" -> vendorRepository.findById(userId)
                    .map(vendorMapper::toResponse)
                    .map(dto -> ResponseEntity.ok((Object) dto))
                    .orElseGet(() -> ResponseEntity.notFound().build());
            default -> ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        };
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new AccessDeniedException("Unauthorized");
        }
        return userId;
    }

    private boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals( "ROLE_" + role));
    }
}
