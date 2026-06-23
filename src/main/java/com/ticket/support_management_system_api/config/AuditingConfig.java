package com.ticket.support_management_system_api.config;

import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfig {

    @Bean
    AuditorAware<UUID> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return Optional.empty();
            }
            Object principal = auth.getPrincipal();
            if (principal instanceof JwtPrincipal jwtPrincipal) {
                return Optional.of(jwtPrincipal.userId());
            }
            return Optional.empty();
        };
    }
}
