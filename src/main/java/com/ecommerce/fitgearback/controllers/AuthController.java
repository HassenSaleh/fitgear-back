package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.login.JwtRequest;
import com.ecommerce.fitgearback.dtos.login.JwtResponse;
import com.ecommerce.fitgearback.services.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173" , allowCredentials = "true")
@Setter
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")

public class AuthController {
    private final AuthService authService;

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
    @GetMapping("/test")
    public ResponseEntity<Object>  test() {
        return (ResponseEntity<Object>) authService.getCurrentUserInfo();
    }
}
