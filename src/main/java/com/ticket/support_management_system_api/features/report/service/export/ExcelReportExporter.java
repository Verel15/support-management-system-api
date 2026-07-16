package com.ticket.support_management_system_api.features.report.service.export;

import com.ticket.support_management_system_api.features.report.dto.ReportField;
import com.ticket.support_management_system_api.features.report.dto.ReportTicketRow;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ExcelReportExporter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] export(List<ReportTicketRow> rows, List<ReportField> fields) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Ticket Report");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < fields.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(ReportFieldLabels.label(fields.get(i)));
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (ReportTicketRow row : rows) {
                Row excelRow = sheet.createRow(rowIdx++);
                for (int i = 0; i < fields.size(); i++) {
                    excelRow.createCell(i).setCellValue(cellValue(row, fields.get(i)));
                }
            }

            for (int i = 0; i < fields.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("สร้างไฟล์ Excel ไม่สำเร็จ", e);
        }
    }

    private String cellValue(ReportTicketRow row, ReportField field) {
        Object value = switch (field) {
            case ticketId -> row.getTicketId();
            case title -> row.getTitle();
            case projectName -> row.getProjectName();
            case companyName -> row.getCompanyName();
            case assigneesDisplay -> row.getAssigneesDisplay();
            case priorityName -> row.getPriorityName();
            case categoryName -> row.getCategoryName();
            case currentStatusName -> row.getCurrentStatusName();
            case createdAt -> row.getCreatedAt() == null ? null : row.getCreatedAt().format(DATE_FORMAT);
            case resolvedAt -> row.getResolvedAt() == null ? null : row.getResolvedAt().format(DATE_FORMAT);
            case resolutionHours -> row.getResolutionHours();
            case overdue -> row.isOverdue() ? "เกินกำหนด" : "ปกติ";
        };
        return value == null ? "" : String.valueOf(value);
    }
}
