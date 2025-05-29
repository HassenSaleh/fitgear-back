package com.ecommerce.fitgearback.dtos.login;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String refreshToken;
    private List<String> roles;
}
