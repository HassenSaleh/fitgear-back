package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.dtos.CustomerDto;
import com.ecommerce.fitgearback.dtos.CustomerResponseDto;
import com.ecommerce.fitgearback.dtos.login.LoginRequestDto;
import com.ecommerce.fitgearback.dtos.login.LoginResponseDto;
import com.ecommerce.fitgearback.exceptions.ResourceNotFoundException;
import com.ecommerce.fitgearback.mappers.UserMapper;
import com.ecommerce.fitgearback.models.Customer;
import com.ecommerce.fitgearback.models.Role;
import com.ecommerce.fitgearback.repositories.CustomerRepository;
import com.ecommerce.fitgearback.repositories.RoleRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Setter
@Getter
@Service
public class CustomerService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper customerMapper;
    private final RoleRepository roleRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = customerRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("User not found with email: " + email));
        return new User(
                user.getEmail(),
                user.getPassword(),
                Collections.emptyList()
        );
    }

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

    public CustomerResponseDto updateCustomer(Long id, CustomerDto dto) {
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

    public LoginResponseDto login(LoginRequestDto loginRequest) {
        Customer customer = customerRepository
                .findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with email: " + loginRequest.getEmail()));

        if (passwordEncoder.matches(loginRequest.getPassword(), customer.getPassword())) {
            return new LoginResponseDto(
                    customer.getId(),
                    customer.getEmail(),
                    "CUSTOMER",
                    "Login successful"
            );
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }
}
