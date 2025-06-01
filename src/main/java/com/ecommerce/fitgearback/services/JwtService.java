package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.Configurations.JwtConfig;
import com.ecommerce.fitgearback.models.JwtUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Service
public class JwtService {
    private final JwtConfig config;

    public String generateAccessToken(JwtUser user, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("name", user.getFirstName() + " " + user.getLastName());
        return createToken(claims, user.getEmail(), config.getAccessTokenExpiration());
    }

    public String generateRefreshToken(JwtUser user, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        return createToken(claims, user.getEmail(), config.getRefreshTokenExpiration());
    }

    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(config.getSecretKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(config.getSecretKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(config.getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            return null;
        }
    }

    public String getEmailFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }
}