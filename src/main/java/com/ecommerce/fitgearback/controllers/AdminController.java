package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.admin.AdminRequest;
import com.ecommerce.fitgearback.dtos.admin.AdminResponse;
import com.ecommerce.fitgearback.services.AdminService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Setter
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/admins")
@PreAuthorize(value = "hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/register")
    public ResponseEntity<AdminResponse> registerAdmin(@Valid @RequestBody AdminRequest dto) {
        AdminResponse admin = adminService.registerAdmin(dto);
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
    public ResponseEntity<AdminResponse> updateAdmin(@PathVariable Long id, @Valid @RequestBody AdminRequest dto) {
        AdminResponse admin = adminService.updateAdmin(id, dto);
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
    public ResponseEntity<AdminResponse> getAdminById(@PathVariable Long id) {
        AdminResponse admin = adminService.getAdminById(id);
        if(admin != null ){
            return ResponseEntity.ok(admin);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<Iterable<AdminResponse>> getAllAdmins() {
        Iterable<AdminResponse> admins = adminService.getAllAdmins();
        if(admins.iterator().hasNext()){
            return ResponseEntity.ok(admins);
        }
        return ResponseEntity.noContent().build();
    }

}
