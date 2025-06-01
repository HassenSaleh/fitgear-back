package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.models.Admin;
import com.ecommerce.fitgearback.models.Customer;
import com.ecommerce.fitgearback.models.JwtUser;
import com.ecommerce.fitgearback.models.Vendor;
import com.ecommerce.fitgearback.repositories.AdminRepository;
import com.ecommerce.fitgearback.repositories.CustomerRepository;
import com.ecommerce.fitgearback.repositories.VendorRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserDetailService implements UserDetailsService {


    private final CustomerRepository customerRepository;
    private final AdminRepository adminRepository;
    private final VendorRepository vendorRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {


        return adminRepository.findByEmail(email)
                .map(admin -> createUserDetails(admin, "ADMIN"))
                .orElseGet(() -> vendorRepository.findByEmail(email)
                        .map(vendor -> createUserDetails(vendor, "VENDOR"))
                        .orElseGet(() -> customerRepository.findByEmail(email)
                                .map(customer -> createUserDetails(customer, "CUSTOMER"))
                                .orElseThrow(() -> {
                                    return new UsernameNotFoundException("User not found with email: " + email);
                                })));
    }

    private UserDetails createUserDetails(Object entity, String role) {
        String email;
        String password;
        Long id;

        if (entity instanceof Admin admin) {
            email = admin.getEmail();
            password = admin.getPassword();
            id = admin.getId();
        } else if (entity instanceof Vendor vendor) {
            email = vendor.getEmail();
            password = vendor.getPassword();
            id = vendor.getId();
        } else if (entity instanceof Customer customer) {
            email = customer.getEmail();
            password = customer.getPassword();
            id = customer.getId();
        } else {
            throw new IllegalStateException("Unknown entity type");
        }

        var authorities = entity instanceof Admin admin
                ? admin.getRoles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName())).collect(Collectors.toList())
                : entity instanceof Vendor vendor
                ? vendor.getRoles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName())).collect(Collectors.toList())
                : ((Customer) entity).getRoles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName())).collect(Collectors.toList());

        return new User(id.toString(), password, authorities);
    }

    private UserDetails findUserById(Long userId) {
        JwtUser user = adminRepository.findById(userId)
                .map(JwtUser.class::cast)
                .orElseGet(() -> vendorRepository.findById(userId)
                        .map(JwtUser.class::cast)
                        .orElseGet(() -> customerRepository.findById(userId)
                                .map(JwtUser.class::cast)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId))));
        return User.builder()
                .username(user.getId().toString())
                .password(user.getPassword())
                .roles(user.getRole().replace("ROLE_", ""))
                .build();
    }

    private UserDetails findUserByEmail(String email) {
        JwtUser user = adminRepository.findByEmail(email)
                .map(JwtUser.class::cast)
                .orElseGet(() -> vendorRepository.findByEmail(email)
                        .map(JwtUser.class::cast)
                        .orElseGet(() -> customerRepository.findByEmail(email)
                                .map(JwtUser.class::cast)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email))));
        return User.builder()
                .username(user.getId().toString())
                .password(user.getPassword())
                .roles(user.getRole().replace("ROLE_", ""))
                .build();
    }
}