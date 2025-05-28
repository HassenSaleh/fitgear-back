package com.ecommerce.fitgearback.dtos.login;

//import com.ecommerce.fitgearback.models.sercurity.Jwt;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class JwtResponse {
    private String token;
//    private String refreshToken;
//    private String role;
//    private Long userId;
//    private String email;
}
