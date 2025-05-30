package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.Configurations.JwtConfig;
import com.ecommerce.fitgearback.models.Jwt;
import com.ecommerce.fitgearback.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class JwtService {
    private final JwtConfig config;
    public String generateAccessToken(User user) {
        return generateToken(user, config.getAccessTokenExpiration());
    }
    public String generateRefreshToken(User user){
        return generateToken(user, config.getRefreshTokenExpiration());
    }

    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("roles", List.class);
    }

    private @Nullable String generateToken(@NotNull User user, long tokenExpiration) {
        if (user.getRoles().isEmpty()) {
            return null;
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoleNames());
        claims.put("userId", user.getId());
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
            return !claims.getExpiration().after(new Date());
        } catch (JwtException ex){
            return true;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(config.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Jwt parseToken(String token) {
        try {
            var claims = getClaims(token);
            return new Jwt(claims, config.getSecretKey());
        } catch (JwtException e) {
            return null;
        }
    }

    public Long getUserIdFromToken(String token){
        return Long.valueOf(getClaims(token).getSubject());
    }
}
