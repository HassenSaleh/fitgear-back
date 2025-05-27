//package com.ecommerce.fitgearback.controllers;
//
//import com.ecommerce.fitgearback.services.UserService;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//@Setter
//@Getter
//
//@AllArgsConstructor
//@RestController
//@RequestMapping("/auth")
//public class AuthController {
//    private final UserService userService;
//
//
//
//    @PostMapping("/register")
//    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
//        String message = userService.registerUser(request);
//        return ResponseEntity.ok(message);
//    }
//}
