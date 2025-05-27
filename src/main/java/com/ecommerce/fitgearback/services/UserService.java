package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.dtos.*;
import com.ecommerce.fitgearback.models.*;
import com.ecommerce.fitgearback.repositories.RoleRepository;
import com.ecommerce.fitgearback.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.*;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Transactional
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User registerCustomer(@NotNull CustomerDto dto) {
        Customer customer = new Customer();
        customer.setEmail(dto.getEmail());
        customer.setPassword(passwordEncoder.encode(dto.getPassword()));
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setShippingAddress(dto.getShippingAddress());
        customer.setPhoneNumber(dto.getPhoneNumber());

        Role role = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        customer.setRoles(Set.of(role));

        return userRepository.save(customer);
    }

    public User registerVendor(@NotNull VendorDto dto) {
        Vendor vendor = new Vendor();
        vendor.setEmail(dto.getEmail());
        vendor.setPassword(passwordEncoder.encode(dto.getPassword()));
        vendor.setFirstName(dto.getFirstName());
        vendor.setLastName(dto.getLastName());
        vendor.setCompanyName(dto.getCompanyName());
        vendor.setCompanyAddress(dto.getCompanyAddress());
        vendor.setContactNumber(dto.getContactNumber());

        Role role = roleRepository.findByName("VENDOR")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        vendor.setRoles(Set.of(role));

        return userRepository.save(vendor);
    }

    public User registerAdmin(@NotNull AdminDto dto) {
        Admin admin = new Admin();
        admin.setEmail(dto.getEmail());
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        admin.setFirstName(dto.getFirstName());
        admin.setLastName(dto.getLastName());
        admin.setDepartment(dto.getDepartment());

        Role role = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        admin.setRoles(Set.of(role));

        return userRepository.save(admin);
    }

    public User registerGeneralUser(RegisterUserRequest req) {
        Role role = roleRepository.findByName(req.getRole().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user;
        switch (role.getName()) {
            case "CUSTOMER":
                Customer customer = new Customer();
                customer.setFirstName(req.getFirstName());
                customer.setLastName(req.getLastName());
                customer.setEmail(req.getEmail());
                customer.setPassword(passwordEncoder.encode(req.getPassword()));
                customer.setRoles(Set.of(role));
                user = customer;
                break;
            case "VENDOR":
                Vendor vendor = new Vendor();
                vendor.setFirstName(req.getFirstName());
                vendor.setLastName(req.getLastName());
                vendor.setEmail(req.getEmail());
                vendor.setPassword(passwordEncoder.encode(req.getPassword()));
                vendor.setRoles(Set.of(role));
                user = vendor;
                break;
            case "ADMIN":
                Admin admin = new Admin();
                admin.setFirstName(req.getFirstName());
                admin.setLastName(req.getLastName());
                admin.setEmail(req.getEmail());
                admin.setPassword(passwordEncoder.encode(req.getPassword()));
                admin.setRoles(Set.of(role));
                user = admin;
                break;
            default:
                throw new RuntimeException("Unsupported role: " + req.getRole());
        }

        return userRepository.save(user);
    }


    private void setCommonFields(User user, UserDto dto) {
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
    }

    private Role getRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
    }

    public void delete(User user) {
        userRepository.delete(user);
    }
    public Iterable<User> findAll() {
        return userRepository.findAll();
    }
}
