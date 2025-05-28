package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.AdminDto;
import com.ecommerce.fitgearback.dtos.AdminResponseDto;
import com.ecommerce.fitgearback.dtos.login.LoginRequestDto;
import com.ecommerce.fitgearback.dtos.login.LoginResponseDto;
import com.ecommerce.fitgearback.models.User;
import com.ecommerce.fitgearback.services.AdminService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Setter
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/register")
    public ResponseEntity<AdminResponseDto> registerAdmin(@Valid @RequestBody AdminDto dto) {
        AdminResponseDto admin = adminService.registerAdmin(dto);
        if(admin != null ){
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/users/{id}")
                    .buildAndExpand(admin.getId())
                    .toUri();
            return ResponseEntity.created(location).body(admin);

        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminResponseDto> updateAdmin(@PathVariable Long id, @Valid @RequestBody AdminDto dto) {
        AdminResponseDto admin = adminService.updateAdmin(id, dto);
        if(admin == null ){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(admin);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminResponseDto> getAdminById(@PathVariable Long id) {
        AdminResponseDto admin = adminService.getAdminById(id);
        if(admin != null ){
            return ResponseEntity.ok(admin);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<Iterable<AdminResponseDto>> getAllAdmins() {
        Iterable<AdminResponseDto> admins = adminService.getAllAdmins();
        if(admins.iterator().hasNext()){
            return ResponseEntity.ok(admins);
        }
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        LoginResponseDto response = adminService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
}
