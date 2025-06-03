package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.customer.*;
import com.ecommerce.fitgearback.dtos.login.JwtRequest;
import com.ecommerce.fitgearback.dtos.login.JwtResponse;
import com.ecommerce.fitgearback.dtos.product.ProductResponse;
import com.ecommerce.fitgearback.dtos.signup.SignUpResponse;
import com.ecommerce.fitgearback.mappers.CustomerMapper;
import com.ecommerce.fitgearback.services.AuthService;
import com.ecommerce.fitgearback.services.CustomerService;
import com.ecommerce.fitgearback.services.OrderService;
import com.ecommerce.fitgearback.services.ProductService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@CrossOrigin(origins = "http://localhost:5173" , allowCredentials = "true")
@PreAuthorize(value = "hasRole('CUSTOMER')")
@Setter
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/customers")

public class CustomerController {
    private final CustomerService customerService;
    private final CustomerMapper customerMapper;
    private final OrderService orderService;
    private final AuthService authService;
    private final ProductService productService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<Page<ProductResponse>> getAll(Pageable pageable) {
        Page<ProductResponse> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @PreAuthorize(value = "hasRole('CUSTOMER')")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        CustomerResponse customer = customerService.getCustomerById(id);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(customer);
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/sign")
    public ResponseEntity<SignUpResponse> sign(@Valid @RequestBody CustomerRequest request, HttpServletResponse response) {
        CustomerResponse customerResponse = customerService.sign(request);
        if (customerResponse == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        JwtResponse jwtResponse = authService.login(new JwtRequest(customerResponse.getEmail(), request.getPassword()), response);
        SignUpResponse signUpResponse = new SignUpResponse(jwtResponse.getAccessToken(), "/auth/login");

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(customerResponse.getId())
                .toUri();
        return ResponseEntity.created(location).body(signUpResponse);
    }

    @PreAuthorize(value = "hasRole('CUSTOMER')")
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        CustomerResponse updatedCustomer = customerService.updateCustomer(id, request);
        if (updatedCustomer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedCustomer);
    }

    @PreAuthorize(value = "hasRole('CUSTOMER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }


    @PreAuthorize(value = "hasRole('CUSTOMER')")
    @GetMapping("/dashboard")
    public ResponseEntity<CustomerResponse> getDashboard() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CustomerResponse customer = customerService.getCustomerById(userId);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(customer);
    }

}
