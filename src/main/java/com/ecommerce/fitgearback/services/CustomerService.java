package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.dtos.CustomerDto;
import com.ecommerce.fitgearback.dtos.CustomerResponseDto;
import com.ecommerce.fitgearback.mappers.CustomerMapper;
import com.ecommerce.fitgearback.mappers.UserMapper;
import com.ecommerce.fitgearback.models.Customer;
import com.ecommerce.fitgearback.models.Role;
import com.ecommerce.fitgearback.repositories.CustomerRepository;
import com.ecommerce.fitgearback.repositories.RoleRepository;
import com.ecommerce.fitgearback.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Setter
@Getter
@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper customerMapper;
    private final RoleRepository roleRepository;

    public CustomerResponseDto registerCustomer(CustomerDto dto) {
        Customer customer = customerMapper.toCustomer(dto);
        customer.setPassword(passwordEncoder.encode(dto.getPassword()));
        Role role = roleRepository
                .findByName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        customer.setRoles(Set.of(role));
        Customer saved = customerRepository.save(customer);
        return customerMapper.toCustomerResponseDTO(saved);
    }


    public List<CustomerResponseDto> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toCustomerResponseDTO)
                .collect(Collectors.toList());
    }
}
