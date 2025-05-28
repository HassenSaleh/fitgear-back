package com.ecommerce.fitgearback.dtos.login;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private Long id;
    private String token;
    private String email;
    private String role; // e.g. "ADMIN", "VENDOR", "CUSTOMER"
    private String message;
}
