package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.login.JwtRequest;
import com.ecommerce.fitgearback.dtos.login.JwtResponse;
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
import org.springframework.web.bind.annotation.*;

@Setter
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    @PostMapping("login")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody JwtRequest request){
                authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
              var token =  jwtService.generateToken(request.getEmail());
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/validate")
    public boolean validate(@RequestHeader("Authorization") String authHeader) {
        var authToken = authHeader.replace("Bearer ", "");
        return jwtService.validateToken(authToken);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentialsException(){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
