package com.ecommerce.fitgearback.dtos.signup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpResponse {
    private String accessToken;
    private String redirectUrl;
}