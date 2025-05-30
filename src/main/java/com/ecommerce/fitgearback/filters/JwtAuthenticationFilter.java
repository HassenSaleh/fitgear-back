package com.ecommerce.fitgearback.filters;

import com.ecommerce.fitgearback.services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private JwtService jwtService;


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Skip authentication for these public paths
        return path.startsWith("/api/v1/customer/") || path.startsWith("/api/v1/auth/") || path.startsWith("/api/v1/vendor/") || path.startsWith("/api/v1/admin/") || path.startsWith("/api/v1/order/")|| path.startsWith("/api/v1/product/") || path.startsWith("/api/v1/cart/") || path.startsWith("/api/v1/payment/") ;
    }
    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorizedResponse(response,"Missing or malformed Authorization header");
            return;
        }
        var token = authHeader.substring(7);
        try {
            if (jwtService.validateToken(token)) {
                Long userId = jwtService.getUserIdFromToken(token);
                var roles = jwtService.getRolesFromToken(token); // Implement this
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();
                var authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        authorities
                );
                authentication.setDetails(new WebAuthenticationDetailsSource()
                        .buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        }catch (Exception e){
            sendUnauthorizedResponse(response, "Invalid token: " + e.getMessage());

        }
    }

    private void sendUnauthorizedResponse(@NotNull HttpServletResponse response , String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(
                Map.of("error", message)
        ));
        response.getWriter().flush();
    }
}
