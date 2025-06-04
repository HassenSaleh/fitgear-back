

package com.ecommerce.fitgearback.filters;
import com.ecommerce.fitgearback.services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@AllArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private static final Logger LOGGER = Logger.getLogger(JwtAuthenticationFilter.class.getName());

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auths/login") ||
                path.startsWith("/api/v1/auths/refresh") ||
                path.startsWith("/api/v1/customers/sign") ||
                path.startsWith("/api/v1/vendors/sign") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            LOGGER.info("No Bearer token found in Authorization header");
            filterChain.doFilter(request, response);
            return;
        }

        var token = authHeader.replace("Bearer ", "");
        LOGGER.info("Processing token: " + token.substring(0, Math.min(token.length(), 10)) + "...");
        try {
            if (!jwtService.validateToken(token)) {
                LOGGER.warning("Token validation failed");
                sendUnauthorizedResponse(response, "Invalid token");
                return;
            }

            Claims claims = jwtService.parseToken(token);
            if (claims == null) {
                LOGGER.warning("Null claims returned from token parsing");
                sendUnauthorizedResponse(response, "Invalid token claims");
                return;
            }

            LOGGER.info("Claims: " + claims);
            String email = claims.getSubject();
            String role = claims.get("role", String.class);
            Object userIdClaim = claims.get("userId");
            Long userId;

            if (userIdClaim instanceof Number) {
                userId = ((Number) userIdClaim).longValue();
            } else if (userIdClaim instanceof String) {
                userId = Long.valueOf((String) userIdClaim);
            } else {
                LOGGER.warning("Invalid userId claim type: " + (userIdClaim != null ? userIdClaim.getClass() : "null"));
                sendUnauthorizedResponse(response, "Invalid userId claim type");
                return;
            }


            if (email == null || role == null) {
                LOGGER.warning("Missing email or role in claims");
                sendUnauthorizedResponse(response, "Missing required claims");
                return;
            }

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            var auth = new UsernamePasswordAuthenticationToken(email, null, authorities); // Use email as principal
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
            LOGGER.info("Authentication set for email: " + email + ", role: " + role);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            LOGGER.severe("Token processing error: " + e.getMessage());
            sendUnauthorizedResponse(response, "Invalid token: " + e.getMessage());
        }
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(
                Map.of("error", message)
        ));
        response.getWriter().flush();
    }
}