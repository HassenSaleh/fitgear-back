package com.ecommerce.fitgearback.payment;

import lombok.Data;

@Data
public class JwtUserPrincipal {
    private final Long userId;
    private final String email;
    private final String role;
}
