package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.CustomerDto;
import com.ecommerce.fitgearback.dtos.CustomerResponseDto;
import com.ecommerce.fitgearback.mappers.UserMapper;
import com.ecommerce.fitgearback.services.CustomerService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
@Setter
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/customer")
public class CustomerController {
    private final CustomerService customerService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<CustomerResponseDto> registerCustomer(@Valid @RequestBody CustomerDto dto) {
        CustomerResponseDto responseDto = customerService.registerCustomer(dto);
        if (responseDto != null) {
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/users/{id}")
                    .buildAndExpand(responseDto.getId())
                    .toUri();
            return ResponseEntity.created(location).body(responseDto);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerDto dto) {
        CustomerResponseDto updatedCustomer = customerService.updateCustomer(id, dto);
        if (updatedCustomer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedCustomer);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponseDto>> getAll() {
        List<CustomerResponseDto> customers = customerService.getAllCustomers();

        if (customers.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(customers);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

//    @PostMapping("/login")
//    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
//        LoginResponseDto response = customerService.login(loginRequest);
//        return ResponseEntity.ok(response);
//    }
}
