package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.customer.*;
import com.ecommerce.fitgearback.mappers.CustomerMapper;
import com.ecommerce.fitgearback.services.CustomerService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
@Setter
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/customer")

@PreAuthorize(value = "hasRole('CUSTOMER')")

public class CustomerController {
    private final CustomerService customerService;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;

    @PreAuthorize(value = "hasRole('CUSTOMER')")

    @GetMapping("/login")
    public ResponseEntity<List<CustomerResponse>> getAll() {
        List<CustomerResponse> customers = customerService.getAllCustomers();

        if (customers.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(customers);
        }
    }

    @PermitAll
    @PostMapping("/sign")
    public ResponseEntity<CustomerResponse> sign(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.sign(request);
        if (response != null) {
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/customer/{id}")
                    .buildAndExpand(response.getId())
                    .toUri();
            return ResponseEntity.created(location).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        CustomerResponse updatedCustomer = customerService.updateCustomer(id, request);
        if (updatedCustomer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedCustomer);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
