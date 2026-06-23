package com.ticket.support_management_system_api.features.auth;

import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.features.auth.dto.LoginRequest;
import com.ticket.support_management_system_api.features.auth.dto.LoginResponse;
import com.ticket.support_management_system_api.features.auth.dto.TokenRefreshRequest;
import com.ticket.support_management_system_api.features.auth.dto.TokenRefreshResponse;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        return ResponseEntity.ok(ApiResponse.success("เข้าสู่ระบบสำเร็จ",
                authService.login(request, httpRequest, httpResponse)));
    }

    @PostMapping("/refresh")
    ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse,
            @RequestBody(required = false) TokenRefreshRequest body) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(httpRequest, httpResponse, body)));
    }

    @PostMapping("/logout")
    ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal JwtPrincipal principal,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        authService.logout(principal, httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.success("ออกจากระบบสำเร็จ", null));
    }
}
