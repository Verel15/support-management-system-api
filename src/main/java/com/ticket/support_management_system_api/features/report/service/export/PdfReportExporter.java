package com.ticket.support_management_system_api.features.report.service.export;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.ticket.support_management_system_api.features.report.dto.PdfExportContext;
import com.ticket.support_management_system_api.features.report.dto.ReportField;
import com.ticket.support_management_system_api.features.report.dto.ReportTicketRow;
import com.ticket.support_management_system_api.features.status.enums.EStatusGroup;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class PdfReportExporter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color BRAND_TEAL = new Color(0x0F, 0x9B, 0x8E);
    private static final Color BRAND_GREEN = new Color(0x22, 0xC5, 0x5E);
    private static final Color BRAND_BLUE = new Color(0x25, 0x63, 0xEB);
    private static final Color BRAND_ORANGE = new Color(0xB4, 0x5F, 0x06);
    private static final Color TABLE_HEADER_BG = new Color(0x1F, 0x29, 0x37);
    private static final Color MUTED_TEXT = new Color(0x6B, 0x72, 0x80);
    private static final Color BORDER_GRAY = new Color(0xE5, 0xE7, 0xEB);
    private static final Color PANEL_BG = new Color(0xF9, 0xFA, 0xFB);
    private static final Color BAR_TRACK_BG = new Color(0xEE, 0xF0, 0xF3);
    private static final String LOGO_CLASSPATH = "static/images/logo-sms.png";
    private static final String FONT_REGULAR_CLASSPATH = "static/fonts/IBMPlexSansThai-Regular.ttf";
    private static final String FONT_BOLD_CLASSPATH = "static/fonts/IBMPlexSansThai-Bold.ttf";

    private static final BaseFont BASE_REGULAR = loadBaseFont(FONT_REGULAR_CLASSPATH);
    private static final BaseFont BASE_BOLD = loadBaseFont(FONT_BOLD_CLASSPATH);

    private static final Font FONT_LOGO_TITLE = thaiFont(BASE_BOLD, 12, Color.DARK_GRAY);
    private static final Font FONT_LOGO_SUB = thaiFont(BASE_REGULAR, 8, MUTED_TEXT);
    private static final Font FONT_H1 = thaiFont(BASE_BOLD, 15, new Color(0x11, 0x18, 0x27));
    private static final Font FONT_H1_SUB = thaiFont(BASE_REGULAR, 8, MUTED_TEXT);
    private static final Font FONT_BANNER = thaiFont(BASE_BOLD, 8, new Color(0x1E, 0x40, 0xAF));
    private static final Font FONT_STAT_LABEL = thaiFont(BASE_REGULAR, 6.5f, MUTED_TEXT);
    private static final Font FONT_STAT_NUMBER = thaiFont(BASE_BOLD, 16, Color.DARK_GRAY);
    private static final Font FONT_STAT_SUB = thaiFont(BASE_REGULAR, 6.5f, MUTED_TEXT);
    private static final Font FONT_SECTION_TITLE = thaiFont(BASE_BOLD, 9, Color.DARK_GRAY);
    private static final Font FONT_PANEL_LABEL = thaiFont(BASE_REGULAR, 8, Color.DARK_GRAY);
    private static final Font FONT_PANEL_VALUE = thaiFont(BASE_BOLD, 8, Color.DARK_GRAY);
    private static final Font FONT_BAR_NAME = thaiFont(BASE_REGULAR, 7.5f, Color.DARK_GRAY);
    private static final Font FONT_BAR_VALUE = thaiFont(BASE_REGULAR, 7.5f, MUTED_TEXT);
    private static final Font FONT_TABLE_HEADER = thaiFont(BASE_BOLD, 7.5f, Color.WHITE);
    private static final Font FONT_TABLE_CELL = thaiFont(BASE_REGULAR, 7.5f, Color.DARK_GRAY);
    private static final Font FONT_TABLE_CELL_BOLD = thaiFont(BASE_BOLD, 7.5f, BRAND_TEAL);
    private static final Font FONT_BADGE = thaiFont(BASE_BOLD, 7, Color.WHITE);
    private static final Font FONT_FOOTER_SMALL = thaiFont(BASE_REGULAR, 7, MUTED_TEXT);

    private static BaseFont loadBaseFont(String classpath) {
        try {
            byte[] bytes = new ClassPathResource(classpath).getInputStream().readAllBytes();
            return BaseFont.createFont(classpath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes, null);
        } catch (IOException | DocumentException e) {
            throw new IllegalStateException("โหลดฟอนต์ไทยไม่สำเร็จ: " + classpath, e);
        }
    }

    private static Font thaiFont(BaseFont baseFont, float size, Color color) {
        return new Font(baseFont, size, Font.NORMAL, color);
    }

    public byte[] export(List<ReportTicketRow> rows, List<ReportField> fields, PdfExportContext context) {
        Document document = new Document(PageSize.A4, 28, 28, 24, 28);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, context);
            addDateRangeBanner(document, context);
            addStatCards(document, context);
            addStatusAndCategoryPanels(document, context);
            addTicketTable(document, rows, fields);
            addFooter(document, context);

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("สร้างไฟล์ PDF ไม่สำเร็จ", e);
        }
    }

    private void addHeader(Document document, PdfExportContext context) throws DocumentException {
        PdfPTable header = new PdfPTable(new float[]{1.3f, 1f});
        header.setWidthPercentage(100);
        header.setSpacingAfter(10);

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        PdfPTable logoRow = new PdfPTable(new float[]{1f, 4f});
        logoRow.setWidthPercentage(100);
        logoRow.addCell(borderless(logoImageOrPlaceholder(), Element.ALIGN_LEFT));
        PdfPCell brandText = new PdfPCell();
        brandText.setBorder(Rectangle.NO_BORDER);
        brandText.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph brand = new Paragraph("Support Management System", FONT_LOGO_TITLE);
        brandText.addElement(brand);
        Paragraph brandSub = new Paragraph("Ticket Report", FONT_LOGO_SUB);
        brandSub.setSpacingBefore(1);
        brandText.addElement(brandSub);
        logoRow.addCell(brandText);
        logoCell.addElement(logoRow);
        header.addCell(logoCell);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph title = new Paragraph("รายงานสรุปภาพรวม Ticket", FONT_H1);
        title.setAlignment(Element.ALIGN_RIGHT);
        titleCell.addElement(title);
        Paragraph subtitle = new Paragraph(
                "Exported: " + context.getExportedAt().format(DATE_FORMAT) + " น. · เลขที่ " + context.getDocumentNo(),
                FONT_H1_SUB);
        subtitle.setAlignment(Element.ALIGN_RIGHT);
        subtitle.setSpacingBefore(2);
        titleCell.addElement(subtitle);
        header.addCell(titleCell);

        document.add(header);

        PdfPTable rule = new PdfPTable(1);
        rule.setWidthPercentage(100);
        PdfPCell ruleCell = new PdfPCell();
        ruleCell.setBorder(Rectangle.BOTTOM);
        ruleCell.setBorderColor(BORDER_GRAY);
        ruleCell.setBorderWidth(1f);
        ruleCell.setFixedHeight(2);
        rule.addCell(ruleCell);
        document.add(rule);
    }

    private Image logoImageOrPlaceholder() {
        try {
            byte[] bytes = new ClassPathResource(LOGO_CLASSPATH).getInputStream().readAllBytes();
            Image image = Image.getInstance(bytes);
            image.scaleToFit(36, 36);
            return image;
        } catch (IOException | com.lowagie.text.BadElementException e) {
            return null;
        }
    }

    private PdfPCell borderless(Element element, int alignment) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        if (element instanceof Image image) {
            cell.addElement(image);
        } else if (element instanceof Paragraph paragraph) {
            cell.addElement(paragraph);
        }
        return cell;
    }

    private void addDateRangeBanner(Document document, PdfExportContext context) throws DocumentException {
        PdfPTable banner = new PdfPTable(1);
        banner.setWidthPercentage(100);
        banner.setSpacingAfter(12);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new Color(0xEF, 0xF6, 0xFF));
        cell.setBorderColor(new Color(0xBF, 0xDB, 0xFE));
        cell.setBorderWidth(0.75f);
        cell.setPadding(8);

        Paragraph dateLine = new Paragraph("ช่วงเวลารายงาน: " + context.getDateRangeLabel(), FONT_BANNER);
        cell.addElement(dateLine);

        Paragraph companyLine = new Paragraph("บริษัท: " + context.getCompanyLabel(), FONT_BANNER);
        companyLine.setSpacingBefore(3);
        cell.addElement(companyLine);

        Paragraph projectLine = new Paragraph("โครงการ: " + context.getProjectLabel(), FONT_BANNER);
        projectLine.setSpacingBefore(3);
        cell.addElement(projectLine);

        banner.addCell(cell);
        document.add(banner);
    }

    private void addStatCards(Document document, PdfExportContext context) throws DocumentException {
        PdfPTable stats = new PdfPTable(4);
        stats.setWidthPercentage(100);
        stats.setSpacingAfter(14);
        stats.setWidths(new float[]{1f, 1f, 1f, 1f});

        long closedPercent = context.getTotalTickets() == 0
                ? 0
                : Math.round((context.getClosedTickets() * 100.0) / context.getTotalTickets());

        stats.addCell(statCell("TICKET ทั้งหมด", String.valueOf(context.getTotalTickets()), null));
        stats.addCell(statCell("ปิดงานแล้ว", String.valueOf(context.getClosedTickets()), closedPercent + "% ของทั้งหมด"));
        stats.addCell(statCell("เวลาแก้ไขเฉลี่ย", formatHours(context.getAvgResolutionHours()), null));
        stats.addCell(statCell("SLA COMPLIANCE", context.getSlaCompliancePercent() + "%", null));
        document.add(stats);
    }

    private String formatHours(double hours) {
        if (hours >= 24) {
            return String.format("%.1f วัน", hours / 24.0);
        }
        return String.format("%.1f ชม.", hours);
    }

    private PdfPCell statCell(String label, String value, String sub) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(PANEL_BG);
        cell.setBorderColor(BORDER_GRAY);
        cell.setBorderWidth(0.5f);
        cell.setPadding(10);

        Paragraph labelP = new Paragraph(label, FONT_STAT_LABEL);
        cell.addElement(labelP);

        Paragraph valueP = new Paragraph(value, FONT_STAT_NUMBER);
        valueP.setSpacingBefore(4);
        cell.addElement(valueP);

        if (sub != null) {
            Paragraph subP = new Paragraph(sub, FONT_STAT_SUB);
            subP.setSpacingBefore(3);
            cell.addElement(subP);
        }
        return cell;
    }

    private void addStatusAndCategoryPanels(Document document, PdfExportContext context) throws DocumentException {
        PdfPTable row = new PdfPTable(new float[]{1f, 1f});
        row.setWidthPercentage(100);
        row.setSpacingAfter(16);

        row.addCell(statusBreakdownPanel(context));
        row.addCell(categoryPanel(context));

        document.add(row);
    }

    private PdfPCell statusBreakdownPanel(PdfExportContext context) {
        PdfPCell panel = new PdfPCell();
        panel.setBackgroundColor(PANEL_BG);
        panel.setBorderColor(BORDER_GRAY);
        panel.setBorderWidth(0.5f);
        panel.setPadding(10);

        Paragraph title = new Paragraph("สถานะ Ticket (Status Breakdown)", FONT_SECTION_TITLE);
        title.setSpacingAfter(8);
        panel.addElement(title);

        long total = context.getTotalTickets();
        panel.addElement(statusLine(BRAND_GREEN, "ปิดงานแล้ว (Closed)", context.getClosedTickets(), total));
        panel.addElement(statusLine(BRAND_BLUE, "กำลังดำเนินการ (In Progress)", context.getProcessingTickets(), total));
        panel.addElement(statusLine(BRAND_ORANGE, "รอดำเนินการ (Open)", context.getPendingTickets(), total));

        return panel;
    }

    private Paragraph statusLine(Color dotColor, String label, long count, long total) {
        long percent = total == 0 ? 0 : Math.round((count * 100.0) / total);
        Paragraph p = new Paragraph();
        p.setSpacingAfter(6);
        p.add(new Chunk("● ", new Font(Font.HELVETICA, 8, Font.NORMAL, dotColor)));
        p.add(new Chunk(label, FONT_PANEL_LABEL));
        for (int i = 0; i < 3; i++) {
            p.add(new Chunk("  ", FONT_PANEL_LABEL));
        }
        p.add(new Chunk(count + " · " + percent + "%", FONT_PANEL_VALUE));
        return p;
    }

    private PdfPCell categoryPanel(PdfExportContext context) {
        PdfPCell panel = new PdfPCell();
        panel.setBackgroundColor(PANEL_BG);
        panel.setBorderColor(BORDER_GRAY);
        panel.setBorderWidth(0.5f);
        panel.setPadding(10);

        Paragraph title = new Paragraph("แยกตามประเภทปัญหา (By Category)", FONT_SECTION_TITLE);
        title.setSpacingAfter(8);
        panel.addElement(title);

        List<PdfExportContext.CategoryCount> categories = context.getCategoryCounts();
        if (categories == null || categories.isEmpty()) {
            panel.addElement(new Paragraph("-", FONT_PANEL_LABEL));
            return panel;
        }

        long max = categories.stream().mapToLong(PdfExportContext.CategoryCount::getCount).max().orElse(1);
        int limit = Math.min(categories.size(), 5);
        for (int i = 0; i < limit; i++) {
            panel.addElement(categoryBarRow(categories.get(i), max));
        }
        return panel;
    }

    private PdfPTable categoryBarRow(PdfExportContext.CategoryCount category, long max) {
        PdfPTable row = new PdfPTable(new float[]{1.2f, 3f, 0.6f});
        row.setWidthPercentage(100);
        row.setSpacingAfter(4);

        PdfPCell nameCell = new PdfPCell(new Paragraph(category.getName(), FONT_BAR_NAME));
        nameCell.setBorder(Rectangle.NO_BORDER);
        nameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        row.addCell(nameCell);

        PdfPCell barCell = new PdfPCell();
        barCell.setBorder(Rectangle.NO_BORDER);
        barCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        barCell.setPadding(2);
        PdfPTable barTrack = new PdfPTable(1);
        barTrack.setWidthPercentage(100);
        PdfPCell fillCell = new PdfPCell();
        double ratio = max == 0 ? 0 : (double) category.getCount() / max;
        fillCell.setFixedHeight(9);
        fillCell.setBorder(Rectangle.NO_BORDER);
        fillCell.setBackgroundColor(BRAND_TEAL);
        fillCell.setColspan(1);
        PdfPTable inner = new PdfPTable(new float[]{(float) Math.max(ratio, 0.02), (float) Math.max(1 - ratio, 0.001)});
        inner.setWidthPercentage(100);
        PdfPCell filled = new PdfPCell();
        filled.setFixedHeight(9);
        filled.setBorder(Rectangle.NO_BORDER);
        filled.setBackgroundColor(BRAND_TEAL);
        PdfPCell empty = new PdfPCell();
        empty.setFixedHeight(9);
        empty.setBorder(Rectangle.NO_BORDER);
        empty.setBackgroundColor(BAR_TRACK_BG);
        inner.addCell(filled);
        inner.addCell(empty);
        barTrack.addCell(new PdfPCell(inner) {{
            setBorder(Rectangle.NO_BORDER);
            setPadding(0);
        }});
        barCell.addElement(barTrack);
        row.addCell(barCell);

        PdfPCell valueCell = new PdfPCell(new Paragraph(String.valueOf(category.getCount()), FONT_BAR_VALUE));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        row.addCell(valueCell);

        return row;
    }

    private void addTicketTable(Document document, List<ReportTicketRow> rows, List<ReportField> fields) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("รายการ TICKET (RECENT TICKETS)", FONT_SECTION_TITLE);
        sectionTitle.setSpacingAfter(8);
        document.add(sectionTitle);

        PdfPTable table = new PdfPTable(fields.size());
        table.setWidthPercentage(100);
        table.setSpacingAfter(18);

        for (ReportField field : fields) {
            PdfPCell headerCell = new PdfPCell(new Paragraph(ReportFieldLabels.label(field), FONT_TABLE_HEADER));
            headerCell.setBackgroundColor(TABLE_HEADER_BG);
            headerCell.setPadding(6);
            headerCell.setBorder(Rectangle.NO_BORDER);
            table.addCell(headerCell);
        }

        boolean alternate = false;
        for (ReportTicketRow row : rows) {
            for (ReportField field : fields) {
                if (field == ReportField.currentStatusName) {
                    table.addCell(statusBadgeCell(row));
                    continue;
                }
                boolean emphasize = field == ReportField.ticketId;
                PdfPCell cell = new PdfPCell(new Paragraph(cellValue(row, field), emphasize ? FONT_TABLE_CELL_BOLD : FONT_TABLE_CELL));
                cell.setPadding(6);
                cell.setBorder(Rectangle.BOTTOM);
                cell.setBorderColor(BORDER_GRAY);
                cell.setBorderWidth(0.5f);
                if (alternate) {
                    cell.setBackgroundColor(new Color(0xFA, 0xFA, 0xFA));
                }
                table.addCell(cell);
            }
            alternate = !alternate;
        }

        document.add(table);
    }

    private PdfPCell statusBadgeCell(ReportTicketRow row) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(6);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(BORDER_GRAY);
        cell.setBorderWidth(0.5f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPTable badgeWrap = new PdfPTable(1);
        badgeWrap.setWidthPercentage(80);
        badgeWrap.setHorizontalAlignment(Element.ALIGN_LEFT);
        PdfPCell badge = new PdfPCell(new Paragraph(row.getCurrentStatusName(), FONT_BADGE));
        badge.setBackgroundColor(statusColor(row.getCurrentStatusGroup()));
        badge.setBorder(Rectangle.NO_BORDER);
        badge.setPadding(4);
        badge.setHorizontalAlignment(Element.ALIGN_CENTER);
        badgeWrap.addCell(badge);
        cell.addElement(badgeWrap);
        return cell;
    }

    private Color statusColor(EStatusGroup group) {
        if (group == null) {
            return MUTED_TEXT;
        }
        return switch (group) {
            case SUCCESS -> BRAND_GREEN;
            case PROCESS -> BRAND_BLUE;
            case START -> BRAND_ORANGE;
            case FAILED -> new Color(0xDC, 0x26, 0x26);
        };
    }

    private void addFooter(Document document, PdfExportContext context) throws DocumentException {
        PdfPTable footRule = new PdfPTable(1);
        footRule.setWidthPercentage(100);
        PdfPCell ruleCell = new PdfPCell();
        ruleCell.setBorder(Rectangle.TOP);
        ruleCell.setBorderColor(BORDER_GRAY);
        ruleCell.setBorderWidth(0.5f);
        ruleCell.setFixedHeight(1);
        footRule.addCell(ruleCell);
        document.add(footRule);

        PdfPTable footText = new PdfPTable(new float[]{1f, 1f});
        footText.setWidthPercentage(100);
        footText.setSpacingBefore(6);
        PdfPCell left = new PdfPCell(new Paragraph(
                "เอกสารนี้สร้างขึ้นโดยอัตโนมัติจากระบบ Support Management System — ผู้ EXPORT: " + context.getExporterName(),
                FONT_FOOTER_SMALL));
        left.setBorder(Rectangle.NO_BORDER);
        PdfPCell right = new PdfPCell(new Paragraph("เลขที่เอกสาร " + context.getDocumentNo(), FONT_FOOTER_SMALL));
        right.setBorder(Rectangle.NO_BORDER);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        footText.addCell(left);
        footText.addCell(right);
        document.add(footText);
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
            case createdAt -> row.getCreatedAt() == null ? null : row.getCreatedAt().format(DATE_ONLY_FORMAT);
            case resolvedAt -> row.getResolvedAt() == null ? null : row.getResolvedAt().format(DATE_ONLY_FORMAT);
            case resolutionHours -> row.getResolutionHours();
            case overdue -> row.isOverdue() ? "เกินกำหนด" : "ปกติ";
        };
        return value == null ? "-" : String.valueOf(value);
    }
}
