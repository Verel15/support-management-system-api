package com.ticket.support_management_system_api.features.auth.filter;

import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractBearerToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtService.validateAndGetClaims(token);
                JwtPrincipal principal = jwtService.extractPrincipal(claims);
                List<SimpleGrantedAuthority> authorities = buildAuthorities(principal);

                UsernamePasswordAuthenticationToken auth =
                        UsernamePasswordAuthenticationToken.authenticated(principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | IllegalArgumentException ignored) {
                // Invalid token — continue without authentication; protected endpoints will return 401
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        // SSE: EventSource cannot set headers, accept token via query param for /subscribe only
        if (request.getRequestURI().endsWith("/subscribe")) {
            return request.getParameter("token");
        }
        return null;
    }

    private List<SimpleGrantedAuthority> buildAuthorities(JwtPrincipal principal) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + principal.accountType().name()));
        for (String perm : principal.permissions()) {
            authorities.add(new SimpleGrantedAuthority("PERM_" + perm));
        }
        return authorities;
    }
}
