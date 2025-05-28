package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.Configurations.JwtConfig;
import com.ecommerce.fitgearback.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
@AllArgsConstructor
@Service
public class JwtService {
    private final JwtConfig config;
    public String generateAccessToken(User user){
        return generateToken(user, config.getAccessTokenExpiration());
    }
    public String generateRefreshToken(User user){
        return generateToken(user, config.getRefreshTokenExpiration());
    }

    private String generateToken(User user, long tokenExpiration) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRoles().iterator().next().getName())
                .claim("name", user.getFirstName() + " " + user.getLastName())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * tokenExpiration))
                .signWith(config.getSecretKey())
                .compact();
    }

    public boolean validateToken(String token){
        try {
            var claims = getClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException ex){
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(config.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserIdFromToken(String token){
        return Long.valueOf(getClaims(token).getSubject());
    }
}
