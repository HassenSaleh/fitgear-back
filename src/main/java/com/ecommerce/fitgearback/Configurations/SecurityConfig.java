package com.ecommerce.fitgearback.Configurations;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@AllArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserDetailsService userService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
//                .sessionManagement(c->
//                        c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .csrf(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests(c-> c
//                        .anyRequest().permitAll());

        .sessionManagement(c->
                c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(c-> c
                        .requestMatchers("/carts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users").permitAll()
                        .requestMatchers(HttpMethod.POST,"/customer").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() throws Exception {
        var provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
