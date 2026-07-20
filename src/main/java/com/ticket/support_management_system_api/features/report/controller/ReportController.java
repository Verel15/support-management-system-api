package com.ticket.support_management_system_api.features.report.controller;

import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.report.dto.ReportExportFormat;
import com.ticket.support_management_system_api.features.report.dto.ReportExportRequest;
import com.ticket.support_management_system_api.features.report.dto.ReportFilterRequest;
import com.ticket.support_management_system_api.features.report.dto.ReportSummaryResponse;
import com.ticket.support_management_system_api.features.report.dto.ReportTicketRow;
import com.ticket.support_management_system_api.features.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PERM_dashboardAccess')")
public class ReportController {

    private static final MediaType EXCEL_MEDIA_TYPE =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    private static final DateTimeFormatter FILENAME_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final ReportService reportService;

    @GetMapping("/tickets/summary")
    public ResponseEntity<ApiResponse<ReportSummaryResponse>> getSummary(
            @ModelAttribute ReportFilterRequest filter,
            @AuthenticationPrincipal JwtPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getSummary(filter, user)));
    }

    @GetMapping("/tickets")
    public ResponseEntity<ApiResponse<PageResponse<ReportTicketRow>>> getTickets(
            @ModelAttribute ReportFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal JwtPrincipal user) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(reportService.getTickets(filter, pageable, user)));
    }

    @PostMapping("/tickets/export")
    public ResponseEntity<byte[]> export(
            @Valid @RequestBody ReportExportRequest request,
            @AuthenticationPrincipal JwtPrincipal user) {
        byte[] content = reportService.export(request, user);
        String timestamp = LocalDateTime.now().format(FILENAME_TIMESTAMP);
        boolean isExcel = request.getFormat() == ReportExportFormat.excel;
        String extension = isExcel ? "xlsx" : "pdf";
        MediaType mediaType = isExcel ? EXCEL_MEDIA_TYPE : MediaType.APPLICATION_PDF;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"ticket_report_" + timestamp + "." + extension + "\"")
                .body(content);
    }
}
