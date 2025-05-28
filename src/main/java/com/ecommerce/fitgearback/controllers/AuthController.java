package com.ecommerce.fitgearback.controllers;

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

    @PostMapping("login")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginRequest request){
                authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
                var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
                var token = jwtService.generateToken(user);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/validate")
    public boolean validate(@RequestHeader("Authorization") String authHeader) {
        var authToken = authHeader.replace("Bearer ", "");
        return jwtService.validateToken(authToken);
    }

//    @GetMapping("/email")
//    public String getEmail(@RequestHeader("Authorization") String authHeader) {
//        var authToken = authHeader.replace("Bearer ", "");
//        return jwtService.getUserIdFromToken(authToken);
//    }
//    @GetMapping("/logout")
//    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
//        var authToken = authHeader.replace("Bearer ", "");
//        jwtService.deleteToken(authToken);
//        return ResponseEntity.noContent().build();
//    }

//    @GetMapping("/refresh")
//    public ResponseEntity<JwtResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
//        var authToken = authHeader.replace("Bearer ", "");
//        var email = jwtService.getEmailFromToken(authToken);
//        var token = jwtService.generateToken(email);
//        return ResponseEntity.ok(new JwtResponse(token));
//    }
//    @PostMapping("/signup")
//    public ResponseEntity<LoginResponse> signUp(@Valid @RequestBody LoginRequest loginRequest){
//        return ResponseEntity.ok(new LoginResponse(loginRequest.getEmail()));
//    }


    //ror customer only
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
