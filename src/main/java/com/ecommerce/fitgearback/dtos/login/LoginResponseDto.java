package com.ecommerce.fitgearback.dtos.login;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    private Long id;
    private String email;
    private String role; // e.g. "ADMIN", "VENDOR", "CUSTOMER"
    private String message;
}
