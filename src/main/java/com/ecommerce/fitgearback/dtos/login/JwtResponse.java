package com.ecommerce.fitgearback.dtos.login;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class JwtResponse {
    private String token;
    @JsonIgnore
    private String refreshToken;
    @JsonIgnore
    private List<String> roles;
}
