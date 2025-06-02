package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.admin.AdminRequest;
import com.ecommerce.fitgearback.dtos.admin.AdminResponse;
import com.ecommerce.fitgearback.dtos.login.JwtRequest;
import com.ecommerce.fitgearback.dtos.signup.SignUpResponse;
import com.ecommerce.fitgearback.dtos.vendor.VendorRequest;
import com.ecommerce.fitgearback.dtos.vendor.VendorResponse;
import com.ecommerce.fitgearback.services.AdminService;
import com.ecommerce.fitgearback.services.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@CrossOrigin(origins = "http://localhost:5173" , allowCredentials = "true")

@Setter
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/admins")
@PreAuthorize(value = "hasRole('ADMIN')")

public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;

    @PreAuthorize("permitAll()")
    @PostMapping("/sign")
    public ResponseEntity<SignUpResponse> sign(@Valid @RequestBody AdminRequest request, HttpServletResponse response) {
        AdminResponse adminResponse = adminService.sign(request);
        if (adminResponse != null) {
            var jwtResponse = authService.login(new JwtRequest(adminResponse.getEmail(), request.getPassword()), response);
            SignUpResponse signUpResponse = new SignUpResponse(jwtResponse.getAccessToken(), "/auth/login");

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(adminResponse.getId())
                    .toUri();
            return ResponseEntity.created(location).body(signUpResponse);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PreAuthorize(value = "hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AdminResponse> updateAdmin(@PathVariable Long id, @Valid @RequestBody AdminRequest dto) {
        AdminResponse admin = adminService.updateAdmin(id, dto);
        if(admin == null ){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(admin);
    }

    @PreAuthorize(value = "hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(value = "hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<AdminResponse> getAdminById(@PathVariable Long id) {
        AdminResponse admin = adminService.getAdminById(id);
        if(admin != null ){
            return ResponseEntity.ok(admin);
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize(value = "hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<Iterable<AdminResponse>> getAllAdmins() {
        Iterable<AdminResponse> admins = adminService.getAllAdmins();
        if(admins.iterator().hasNext()){
            return ResponseEntity.ok(admins);
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize(value = "hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<AdminResponse> getDashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();

        AdminResponse adminResponse = adminService.getAdminByUsername(username);

        if (adminResponse == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(adminResponse);
    }

}
