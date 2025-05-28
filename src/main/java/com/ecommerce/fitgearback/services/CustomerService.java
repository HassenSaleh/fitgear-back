package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.dtos.CustomerDto;
import com.ecommerce.fitgearback.dtos.CustomerResponseDto;
import com.ecommerce.fitgearback.exceptions.ResourceNotFoundException;
import com.ecommerce.fitgearback.mappers.UserMapper;
import com.ecommerce.fitgearback.models.Customer;
import com.ecommerce.fitgearback.models.Role;
import com.ecommerce.fitgearback.repositories.CustomerRepository;
import com.ecommerce.fitgearback.repositories.RoleRepository;
import org.jetbrains.annotations.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Setter
@Getter
@Service
public class  CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper customerMapper;
    private final RoleRepository roleRepository;

    public CustomerResponseDto registerCustomer(@NotNull CustomerDto dto) {
        Customer customer = customerMapper.toCustomer(dto);
        customer.setPassword(passwordEncoder.encode(dto.getPassword()));
        Role role = roleRepository
                .findByName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        customer.setRoles(Set.of(role));
        Customer saved = customerRepository.save(customer);
        return customerMapper.toCustomerResponseDTO(saved);
    }

    public CustomerResponseDto updateCustomer(@NotNull Long id, @NotNull CustomerDto dto) {
        Customer existing = customerRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));

        existing.setFirstName(dto.getFirstName());
        existing.setLastName(dto.getLastName());
        existing.setEmail(dto.getEmail());
        existing.setPassword(passwordEncoder.encode(dto.getPassword()));

        Customer updated = customerRepository.save(existing);
        return customerMapper.toCustomerResponseDTO(updated);
    }
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found: " + id);
        }
        customerRepository.deleteById(id);
    }

    public CustomerResponseDto getById(Long id) {
        Customer customer = customerRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        return customerMapper.toCustomerResponseDTO(customer);
    }

    public List<CustomerResponseDto> getAllCustomers() {
        return customerRepository
                .findAll()
                .stream()
                .map(customerMapper::toCustomerResponseDTO)
                .collect(Collectors.toList());
    }

//    public LoginResponseDto login(@NotNull LoginRequestDto loginRequest) {
//        Customer customer = customerRepository
//                .findByEmail(loginRequest.getEmail())
//                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with email: " + loginRequest.getEmail()));
//
//        if (passwordEncoder.matches(loginRequest.getPassword(), customer.getPassword())) {
//            return new LoginResponseDto(
//                    customer.getId(),
//                    customer.getEmail(),
//                    "CUSTOMER",
//                    "Login successful"
//            );
//        } else {
//            throw new RuntimeException("Invalid credentials");
//        }
//    }
}
