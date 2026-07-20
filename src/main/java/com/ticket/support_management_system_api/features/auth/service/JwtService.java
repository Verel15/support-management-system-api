package com.ticket.support_management_system_api.features.auth.service;

import com.ticket.support_management_system_api.common.enums.AccountType;
import com.ticket.support_management_system_api.config.JwtProperties;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.user.entities.User;
import com.ticket.support_management_system_api.features.user_type.entities.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    private static final List<String> ALL_PERMISSIONS = List.of(
            "allProjectAccess", "notificationAccess", "dashboardAccess", "allTicketAccess",
            "manageProjectAccess", "manageUserAccess", "manageCompanyAccess", "manageDataAccess");

    public String generateAccessToken(User user, UserType userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("accountType", user.getAccountType().name());

        if (user.getAccountType() == AccountType.ADMIN) {
            claims.put("permissions", ALL_PERMISSIONS);
        } else if (userType != null) {
            claims.put("userTypeId", userType.getId().toString());
            claims.put("permissions", extractPermissions(userType));
        } else {
            claims.put("permissions", List.of());
        }

        return Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuer(jwtProperties.getIssuer())
                .audience().add(jwtProperties.getAudience()).and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpiration()))
                .signWith(signingKey())
                .compact();
    }

    public Claims validateAndGetClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public JwtPrincipal extractPrincipal(Claims claims) {
        UUID userId = UUID.fromString(claims.getSubject());
        String email = claims.get("email", String.class);
        AccountType accountType = AccountType.valueOf(claims.get("accountType", String.class));

        String userTypeIdStr = claims.get("userTypeId", String.class);
        UUID userTypeId = userTypeIdStr != null ? UUID.fromString(userTypeIdStr) : null;

        @SuppressWarnings("unchecked")
        List<String> permissions = (List<String>) claims.get("permissions", List.class);

        return new JwtPrincipal(userId, email, accountType, userTypeId,
                permissions != null ? permissions : List.of());
    }

    public String generateRefreshTokenValue() {
        return UUID.randomUUID().toString().replace("-", "") +
               UUID.randomUUID().toString().replace("-", "");
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
    }

    private List<String> extractPermissions(UserType userType) {
        List<String> perms = new ArrayList<>();
        if (userType.isAllProjectAccess())    perms.add("allProjectAccess");
        if (userType.isNotificationAccess()) perms.add("notificationAccess");
        if (userType.isDashboardAccess())    perms.add("dashboardAccess");
        if (userType.isAllTicketAccess())    perms.add("allTicketAccess");
        if (userType.isManageProjectAccess()) perms.add("manageProjectAccess");
        if (userType.isManageUserAccess())   perms.add("manageUserAccess");
        if (userType.isManageCompanyAccess()) perms.add("manageCompanyAccess");
        if (userType.isManageDataAccess())   perms.add("manageDataAccess");
        return perms;
    }
}
