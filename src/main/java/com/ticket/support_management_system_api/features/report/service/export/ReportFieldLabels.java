package com.ticket.support_management_system_api.features.report.service.export;

import com.ticket.support_management_system_api.features.report.dto.ReportField;

final class ReportFieldLabels {

    private ReportFieldLabels() {}

    static String label(ReportField field) {
        return switch (field) {
            case ticketId -> "รหัส Ticket";
            case title -> "หัวข้องาน";
            case projectName -> "โครงการ";
            case companyName -> "บริษัท";
            case assigneesDisplay -> "ผู้รับผิดชอบ";
            case priorityName -> "ลำดับความสำคัญ";
            case categoryName -> "ประเภทปัญหา";
            case currentStatusName -> "สถานะ";
            case createdAt -> "วันที่สร้าง";
            case resolvedAt -> "วันที่ปิดงาน";
            case resolutionHours -> "ระยะเวลาแก้ไข (ชม.)";
            case overdue -> "เกินกำหนด";
        };
    }
}
