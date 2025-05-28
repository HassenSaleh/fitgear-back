package com.ecommerce.fitgearback.dtos.login;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class JwtResponse {
    private String token;
}
