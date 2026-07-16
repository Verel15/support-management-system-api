package com.ticket.support_management_system_api.features.report.service.export;

import com.ticket.support_management_system_api.features.report.dto.PdfExportContext;
import com.ticket.support_management_system_api.features.report.dto.ReportField;
import com.ticket.support_management_system_api.features.report.dto.ReportTicketRow;
import com.ticket.support_management_system_api.features.status.enums.EStatusGroup;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PdfReportExporterSmokeTest {

    @Test
    void exportsPdfWithSampleData() throws IOException {
        PdfReportExporter exporter = new PdfReportExporter();

        List<ReportTicketRow> rows = List.of(
                row("TCK-004821", "ไม่สามารถเข้าสู่ระบบ VPN บริษัทได้", "กิตติ วัฒนกุล", "สูง", "Network/VPN", "Closed", EStatusGroup.SUCCESS, false, "บริษัท เอบีซี จำกัด", "โครงการ ระบบบัญชี"),
                row("TCK-004819", "เครื่องพิมพ์ชั้น 5 ใช้งานไม่ได้", "ปิยดา สุขสม, ธนพล อารีย์", "กลาง", "Hardware", "In Progress", EStatusGroup.PROCESS, false, "บริษัท เอบีซี จำกัด", "โครงการ ERP"),
                row("TCK-004810", "อีเมลไม่สามารถส่งไฟล์แบบขนาดใหญ่ได้", "กิตติ วัฒนกุล", "กลาง", "Software", "Open", EStatusGroup.START, true, "บริษัท เอ็กซ์วายแซด จำกัด", "โครงการ อีเมลองค์กร")
        );

        PdfExportContext context = PdfExportContext.builder()
                .documentNo("SMS-EXP-2026-0716-001")
                .exportedAt(LocalDateTime.now())
                .exporterName("Toei Wichayut")
                .companyLabel("บริษัท เอบีซี จำกัด, บริษัท เอ็กซ์วายแซด จำกัด")
                .projectLabel("โครงการ ระบบบัญชี, โครงการ ERP, โครงการ อีเมลองค์กร")
                .dateRangeLabel("01/07/2569 - 15/07/2569")
                .assigneeLabel("ผู้รับผิดชอบทั้งหมด")
                .priorityLabel("ลำดับความสำคัญทั้งหมด")
                .statusLabel("สถานะทั้งหมด")
                .totalTickets(248)
                .closedTickets(211)
                .processingTickets(24)
                .pendingTickets(13)
                .avgResolutionHours(1.8)
                .slaCompliancePercent(94.0)
                .categoryCounts(List.of(
                        PdfExportContext.CategoryCount.builder().name("Network/VPN").count(86).build(),
                        PdfExportContext.CategoryCount.builder().name("Hardware").count(61).build(),
                        PdfExportContext.CategoryCount.builder().name("Software").count(49).build(),
                        PdfExportContext.CategoryCount.builder().name("Account/Access").count(35).build(),
                        PdfExportContext.CategoryCount.builder().name("อื่น ๆ").count(17).build()
                ))
                .build();

        List<ReportField> fields = List.of(
                ReportField.ticketId, ReportField.title, ReportField.companyName, ReportField.projectName,
                ReportField.assigneesDisplay, ReportField.priorityName, ReportField.currentStatusName, ReportField.createdAt);

        byte[] pdf = exporter.export(rows, fields, context);

        assertThat(pdf).isNotEmpty();
        try (FileOutputStream fos = new FileOutputStream("/private/tmp/claude-501/-Users-wichayut-java-spring-boot-support-management-system-api/533c2984-16d3-494a-b7a5-8268721ba398/scratchpad/sample-report.pdf")) {
            fos.write(pdf);
        }
    }

    private ReportTicketRow row(String ticketId, String title, String assignee, String priority,
                                 String category, String statusName, EStatusGroup group, boolean overdue,
                                 String companyName, String projectName) {
        return ReportTicketRow.builder()
                .ticketId(ticketId)
                .title(title)
                .companyName(companyName)
                .projectName(projectName)
                .assigneesDisplay(assignee)
                .priorityName(priority)
                .categoryName(category)
                .currentStatusName(statusName)
                .currentStatusGroup(group)
                .createdAt(LocalDateTime.now().minusDays(2))
                .resolvedAt(group == EStatusGroup.SUCCESS ? LocalDateTime.now() : null)
                .resolutionHours(group == EStatusGroup.SUCCESS ? 1.4 : null)
                .overdue(overdue)
                .build();
    }
}
