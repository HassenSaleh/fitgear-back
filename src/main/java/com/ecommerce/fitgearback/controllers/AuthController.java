package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.Configurations.JwtConfig;
import com.ecommerce.fitgearback.dtos.login.JwtRequest;
import com.ecommerce.fitgearback.dtos.login.JwtResponse;
import com.ecommerce.fitgearback.dtos.login.LoginRequest;
import com.ecommerce.fitgearback.mappers.AdminMapper;
import com.ecommerce.fitgearback.mappers.CustomerMapper;
import com.ecommerce.fitgearback.mappers.VendorMapper;
import com.ecommerce.fitgearback.models.Role;
import com.ecommerce.fitgearback.repositories.AdminRepository;
import com.ecommerce.fitgearback.repositories.CustomerRepository;
import com.ecommerce.fitgearback.repositories.UserRepository;
import com.ecommerce.fitgearback.repositories.VendorRepository;
import com.ecommerce.fitgearback.services.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:5173" , allowCredentials = "true")
public class AuthController {
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

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response){
                authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                        )
                );
                var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
                var accessToken = jwtService.generateAccessToken(user);
                var refreshToken = jwtService.generateRefreshToken(user);

                var cookie = new Cookie("refreshToken", refreshToken);
                cookie.setHttpOnly(true);
                cookie.setPath("/auth/refresh");
                cookie.setMaxAge(config.getRefreshTokenExpiration());
                cookie.setSecure(false);
                response.addCookie(cookie);
                var roles = user.getRoles().stream()
                    .map(Role::getName)
                    .toList();
            String redirectUrl = "/login";
            if (roles.contains("ADMIN")) {
                redirectUrl = "/admin/dashboard";
            } else if (roles.contains("VENDOR")) {
                redirectUrl = "/vendor/dashboard";
            } else if (roles.contains("CUSTOMER")) {
                redirectUrl = "/customer/home";
            }
        return ResponseEntity.ok(new JwtResponse(
                accessToken,
                refreshToken,
                roles,
                redirectUrl)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(
            @CookieValue("refreshToken") String refreshToken
    ){
        if(!jwtService.validateToken(refreshToken)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var userId =  jwtService.getUserIdFromToken(refreshToken);
        var user = userRepository.findById(userId).orElseThrow();
        var accessToken = jwtService.generateAccessToken(user);
        var roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();
        String redirectUrl = "/login";
        if (roles.contains("ADMIN")) {
            redirectUrl = "/admin/dashboard";
        } else if (roles.contains("VENDOR")) {
            redirectUrl = "/vendor/dashboard";
        } else if (roles.contains("CUSTOMER")) {
            redirectUrl = "/customer/home";
        }
        return ResponseEntity.ok(new JwtResponse(
                accessToken,
                null,
                roles,
                redirectUrl)
        );
    }
    //works for all now
    @GetMapping("/test")
    public ResponseEntity<?> test(){
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var userId = (Long) auth.getPrincipal();
        var userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        var user = userOpt.get();
        if (user.getRoles().stream().anyMatch(r -> r.getName().equals("CUSTOMER"))) {
            var customer = customerRepository.findById(userId).orElse(null);
            if (customer == null) return ResponseEntity.notFound().build();
            var dto = customerMapper.toResponse(customer);
            return ResponseEntity.ok(dto);
        }
        if (user.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"))) {
            var admin = adminRepository.findById(userId).orElse(null);
            if (admin == null) return ResponseEntity.notFound().build();
            var dto = adminMapper.toResponse(admin);
            return ResponseEntity.ok(dto);
        }

        if (user.getRoles().stream().anyMatch(r -> r.getName().equals("VENDOR"))) {
            var vendor = vendorRepository.findById(userId).orElse(null);
            if (vendor == null) return ResponseEntity.notFound().build();
            var dto = vendorMapper.toResponse(vendor);
            return ResponseEntity.ok(dto);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentialsException(){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
