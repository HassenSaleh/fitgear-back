package com.ecommerce.fitgearback.Configurations;

import com.ecommerce.fitgearback.filters.JwtAuthenticationFilter;
import com.ecommerce.fitgearback.models.Admin;
import com.ecommerce.fitgearback.models.Customer;
import com.ecommerce.fitgearback.models.Vendor;
import com.ecommerce.fitgearback.repositories.AdminRepository;
import com.ecommerce.fitgearback.repositories.CustomerRepository;
import com.ecommerce.fitgearback.repositories.VendorRepository;
import com.ecommerce.fitgearback.services.UserDetailService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Optional;

@AllArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final UserDetailService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationFilter authenticationFilter;
    private final CustomerRepository customerRepository;
    private final AdminRepository adminRepository;
    private final VendorRepository vendorRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(c -> c
                        .requestMatchers("/api/v1/auths/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                        .requestMatchers("/api/products/featured").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/carts/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/v1/orders/**").hasAnyRole("CUSTOMER","VENDOR")
                        .requestMatchers("/api/v1/orders/vendor/**").hasAnyRole("VENDOR","CUSTOMER")
                        .requestMatchers("/api/v1/checkout/**").permitAll()
                        .requestMatchers("/api/v1/payments/**").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasRole("VENDOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasRole("VENDOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("VENDOR")
                        .requestMatchers("/api/v1/admins/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/customers/**").permitAll()
                        .requestMatchers("/api/v1/vendors/**").permitAll()
                        .requestMatchers("/api/v1/customers/sign").permitAll()
                        .requestMatchers("/api/v1/vendors/sign").permitAll()
                        .requestMatchers("/api/v1/auths/login").permitAll()
                        .requestMatchers("/api/v1/auths/refresh").permitAll()
                        .requestMatchers("/api/v1/auths/test").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(c -> c.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Optional<Customer> customer = customerRepository.findByEmail(username);
            if (customer.isPresent()) {
                return User.withUsername(customer.get().getEmail())
                        .password(customer.get().getPassword())
                        .roles("CUSTOMER")
                        .build();
            }
            Optional<Vendor> vendor = vendorRepository.findByEmail(username);
            if (vendor.isPresent()) {
                return User.withUsername(vendor.get().getEmail())
                        .password(vendor.get().getPassword())
                        .roles("VENDOR")
                        .build();
            }
            Optional<Admin> admin = adminRepository.findByEmail(username);
            if (admin.isPresent()) {
                return User.withUsername(admin.get().getEmail())
                        .password(admin.get().getPassword())
                        .roles("ADMIN")
                        .build();
            }
            throw new UsernameNotFoundException("User not found with email: " + username);
        };
    }
}