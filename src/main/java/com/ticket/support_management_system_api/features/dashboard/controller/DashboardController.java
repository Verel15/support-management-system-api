package com.ticket.support_management_system_api.features.dashboard.controller;

import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.features.dashboard.dto.*;
import com.ticket.support_management_system_api.features.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/tickets/status-distribution")
    public ResponseEntity<ApiResponse<TicketStatusDistributionResponse>> getTicketStatusDistribution(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getTicketStatusDistribution(y, m)));
    }

    @GetMapping("/tickets/top-projects")
    public ResponseEntity<ApiResponse<TopProjectResponse>> getTop5Projects(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getTop5ProjectsByTickets(y, m)));
    }

    @GetMapping("/tickets/summary")
    public ResponseEntity<ApiResponse<TicketSummaryResponse>> getTicketSummary(
            @RequestParam(required = false) Integer year) {
        int y = year != null ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getTicketSummary(y)));
    }

    @GetMapping("/projects/status-distribution")
    public ResponseEntity<ApiResponse<ProjectStatusDistributionResponse>> getProjectStatusDistribution(
            @RequestParam(required = false) Integer year) {
        int y = year != null ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getProjectStatusDistribution(y)));
    }

    @GetMapping("/tickets/trend")
    public ResponseEntity<ApiResponse<TicketTrendResponse>> getTicketTrend(
            @RequestParam(required = false) Integer year) {
        int y = year != null ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getTicketTrend(y)));
    }
}
