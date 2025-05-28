package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.Configurations.JwtConfig;
import com.ecommerce.fitgearback.dtos.CustomerResponseDto;
import com.ecommerce.fitgearback.dtos.UserDto;
import com.ecommerce.fitgearback.dtos.login.JwtRequest;
import com.ecommerce.fitgearback.dtos.login.JwtResponse;
import com.ecommerce.fitgearback.dtos.login.LoginRequest;
import com.ecommerce.fitgearback.dtos.login.LoginResponse;
import com.ecommerce.fitgearback.mappers.UserMapper;
import com.ecommerce.fitgearback.models.Admin;
import com.ecommerce.fitgearback.repositories.CustomerRepository;
import com.ecommerce.fitgearback.repositories.UserRepository;
import com.ecommerce.fitgearback.services.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Setter
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final UserMapper mapper;
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
                cookie.setSecure(true);
                response.addCookie(cookie);
        return ResponseEntity.ok(new JwtResponse(accessToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(
            @CookieValue("refreshToken") String refreshToken
    ){
        if(!jwtService.validateToken(refreshToken)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var userId = jwtService.getUserIdFromToken(refreshToken);
        var user = userRepository.findById(userId).orElseThrow();
        var accessToken = jwtService.generateAccessToken(user);
        return ResponseEntity.ok(new JwtResponse(accessToken));
    }

    //for customer only
    @GetMapping("/test")
    public ResponseEntity<CustomerResponseDto> test(){
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var userId = (Long) auth.getPrincipal();
        var user = customerRepository.findById(userId).orElse(null);
        if(user == null){
            return ResponseEntity.notFound().build();
        }
        var ma = mapper.toCustomerResponseDTO(user);
        return ResponseEntity.ok(ma);

    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentialsException(){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
