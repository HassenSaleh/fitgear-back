package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.*;
import com.ecommerce.fitgearback.mappers.UserMapper;

import com.ecommerce.fitgearback.models.User;
import com.ecommerce.fitgearback.services.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
@Setter
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    @GetMapping
    public Iterable<User> getAllUsers(){
        return userService.findAll();
    }

    @PostMapping
    public ResponseEntity<User> register(@Valid @RequestBody RegisterUserRequest request) {
        var user = userService.registerGeneralUser(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(location).body(user);
    }
    @PostMapping("/register/admin")
    public ResponseEntity<User> registerAdmin(@Valid @RequestBody AdminDto dto) {
        User user = userService.registerAdmin(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(location).body(user);
    }

    @GetMapping("/{id}/type")
    public ResponseEntity<String> getUserType(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> ResponseEntity.ok(user.getClass().getSimpleName()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
