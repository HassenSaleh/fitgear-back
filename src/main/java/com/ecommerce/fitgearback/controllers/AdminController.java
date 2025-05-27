package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.AdminDto;
import com.ecommerce.fitgearback.dtos.AdminResponseDto;
import com.ecommerce.fitgearback.models.User;
import com.ecommerce.fitgearback.services.AdminService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
