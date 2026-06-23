package com.ticket.support_management_system_api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application.security.jwt")
@Getter
@Setter
public class JwtProperties {

    private String secret;
    private String issuer;
    private String audience;
    private long accessTokenExpiration = 900000L;
    private long refreshTokenExpiration = 2592000000L;
}
