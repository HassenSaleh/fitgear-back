package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.AdminDto;
import com.ecommerce.fitgearback.dtos.CustomerDto;
import com.ecommerce.fitgearback.dtos.RegisterUserRequest;
import com.ecommerce.fitgearback.dtos.VendorDto;
import com.ecommerce.fitgearback.models.User;
import com.ecommerce.fitgearback.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping
    public Iterable<com.ecommerce.fitgearback.models.User> getAllUsers(){
        return userService.findAll();
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterUserRequest request) {
        var user = userService.registerGeneralUser(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(location).body(user);
    }
    @PostMapping("/register/customer")
    public ResponseEntity<User> registerCustomer( @Valid @RequestBody CustomerDto dto) {
        User user = userService.registerCustomer(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(location).body(user);
    }

    @PostMapping("/register/vendor")
    public ResponseEntity<User> registerVendor(@Valid @RequestBody VendorDto dto) {
        User user = userService.registerVendor(dto);
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
