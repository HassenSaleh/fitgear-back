package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.dtos.customer.*;
import com.ecommerce.fitgearback.exceptions.ResourceNotFoundException;
import com.ecommerce.fitgearback.mappers.CustomerMapper;
import com.ecommerce.fitgearback.models.Customer;
import com.ecommerce.fitgearback.models.Role;
import com.ecommerce.fitgearback.repositories.CustomerRepository;
import com.ecommerce.fitgearback.repositories.RoleRepository;
import org.jetbrains.annotations.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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
    private final CustomerMapper customerMapper;
    private final RoleRepository roleRepository;
    private final AuthService authService;

    public CustomerResponse sign(@NotNull CustomerRequest request) {
        Customer customer = customerMapper.toCustomer(request);
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        Role role = roleRepository
                .findByName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        customer.setRoles(Set.of(role));
        Customer saved = customerRepository.save(customer);
        return customerMapper.toResponse(saved);
    }

    public CustomerResponse updateCustomer(@NotNull Long id, @NotNull CustomerRequest request) {
        Customer existing = customerRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));

        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setEmail(request.getEmail());
        existing.setPassword(passwordEncoder.encode(request.getPassword()));

        Customer updated = customerRepository.save(existing);
        return customerMapper.toResponse(updated);
    }
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found: " + id);
        }
        customerRepository.deleteById(id);
    }


    public CustomerResponse getCustomerById(Long id) {
        Customer currentVendor = authService.getCurrentCustomer();
        if (!currentVendor.getId().equals(id)) {
            throw new AccessDeniedException("You can only view your own orders.");
        }
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return customerMapper.toResponse(customer);
    }

    public CustomerResponse getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
        return customerMapper.toResponse(customer);
    }
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository
                .findAll()
                .stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }
}
