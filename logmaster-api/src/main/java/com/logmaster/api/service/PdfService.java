package com.logmaster.api.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.logmaster.api.model.Delivery;
import com.logmaster.api.model.LogEntry;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class PdfService {

    private static final DeviceRgb GREEN_DARK = new DeviceRgb(27, 94, 32);
    private static final DeviceRgb GREEN_LIGHT = new DeviceRgb(200, 230, 201);
    private static final DeviceRgb GREEN_MID = new DeviceRgb(46, 125, 50);

    private static final Map<String, String> TYPE_LABELS = Map.of(
            "RUBBER_4FT", "4ft Rubber Logs",
            "OTHER_4FT", "4ft Other Logs",
            "RUBBER_3FT", "3ft Rubber Logs",
            "OTHER_3FT", "3ft Other Logs"
    );

    private static final Map<String, String> CAT_LABELS = Map.of(
            "LOW", "Low",
            "MID", "Mid",
            "HIGH", "High"
    );

    public byte[] generateReport(Map<String, Object> summary,
                                 Delivery delivery,
                                 List<LogEntry> entries) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc = new Document(pdfDoc);
            doc.setMargins(36, 36, 36, 36);

            // ── HEADER ────────────────────────────────────────────
            doc.add(new Paragraph("LogMaster — Delivery Report")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(GREEN_DARK));

            doc.add(new Paragraph("Generated: " +
                    LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")))
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY));

            doc.add(new Paragraph(" "));

            // ── DELIVERY INFO ──────────────────────────────────────
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{2, 3}))
                    .useAllAvailableWidth();

            addInfoRow(infoTable, "Supplier", summary.get("supplierName").toString());
            addInfoRow(infoTable, "Date", summary.get("deliveryDate").toString());

            Map<String, Object> thresholds = (Map<String, Object>) summary.get("categoryThresholds");
            addInfoRow(infoTable, "Thresholds",
                    "LOW < " + thresholds.get("lowMax") +
                            " | MID " + thresholds.get("lowMax") + "–" + thresholds.get("midMax") +
                            " | HIGH > " + thresholds.get("midMax"));

            doc.add(infoTable);
            doc.add(new Paragraph(" "));

            // ── PER TYPE TABLES ────────────────────────────────────
            Map<String, Object> byType = (Map<String, Object>) summary.get("byType");

            for (Map.Entry<String, Object> typeEntry : byType.entrySet()) {
                String logType = typeEntry.getKey();
                Map<String, Object> typeData = (Map<String, Object>) typeEntry.getValue();

                doc.add(new Paragraph(TYPE_LABELS.getOrDefault(logType, logType))
                        .setFontSize(13)
                        .setBold()
                        .setFontColor(GREEN_DARK)
                        .setMarginTop(10));

                Table t = new Table(UnitValue.createPercentArray(new float[]{3, 2, 3, 3, 3}))
                        .useAllAvailableWidth();

                // Header
                addTableHeader(t, "Category");
                addTableHeader(t, "Logs");
                addTableHeader(t, "Total Cft");
                addTableHeader(t, "Price/Cft");
                addTableHeader(t, "Total (LKR)");

                Map<String, Object> categories =
                        (Map<String, Object>) typeData.get("categories");

                for (Map.Entry<String, Object> catEntry : categories.entrySet()) {
                    Map<String, Object> catData =
                            (Map<String, Object>) catEntry.getValue();

                    addCell(t, CAT_LABELS.getOrDefault(catEntry.getKey(), catEntry.getKey()));
                    addCell(t, catData.get("logCount").toString());
                    addCell(t, formatNum(catData.get("totalCft"), 2));
                    addCell(t, catData.get("pricePerCft") != null
                            ? formatNum(catData.get("pricePerCft"), 2) : "—");
                    addCell(t, formatNum(catData.get("totalPrice"), 2));
                }

                // Subtotal
                addSubtotalCell(t, "Subtotal");
                addSubtotalCell(t, typeData.get("totalLogs").toString());
                addSubtotalCell(t, formatNum(typeData.get("totalCft"), 2));
                addSubtotalCell(t, "");
                addSubtotalCell(t, formatNum(typeData.get("totalPrice"), 2));

                doc.add(t);
            }

            doc.add(new Paragraph(" "));

            // ── GRAND TOTAL ────────────────────────────────────────
            Table grand = new Table(UnitValue.createPercentArray(new float[]{3, 2, 3, 3, 3}))
                    .useAllAvailableWidth();

            addGrandCell(grand, "GRAND TOTAL");
            addGrandCell(grand, summary.get("grandTotalLogs").toString() + " logs");
            addGrandCell(grand, formatNum(summary.get("grandTotalCft"), 2) + " Cft");
            addGrandCell(grand, "");
            addGrandCell(grand, "LKR " + formatNum(summary.get("grandTotalPrice"), 2));

            doc.add(grand);

            // ── FOOTER ────────────────────────────────────────────
            doc.add(new Paragraph("\nGenerated by LogMaster • Confidential")
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY));

            doc.close();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage());
        }

        return out.toByteArray();
    }

    // ── Helpers ───────────────────────────────────────────────────

    private String formatNum(Object val, int scale) {
        if (val == null) return "0";
        try {
            return new java.math.BigDecimal(val.toString())
                    .setScale(scale, RoundingMode.HALF_UP)
                    .toPlainString();
        } catch (Exception e) {
            return val.toString();
        }
    }

    private void addInfoRow(Table t, String label, String value) {
        t.addCell(new Cell().add(new Paragraph(label).setBold().setFontSize(10)));
        t.addCell(new Cell().add(new Paragraph(value).setFontSize(10)));
    }

    private void addTableHeader(Table t, String text) {
        t.addHeaderCell(new Cell()
                .setBackgroundColor(GREEN_DARK)
                .add(new Paragraph(text)
                        .setBold()
                        .setFontSize(9)
                        .setFontColor(ColorConstants.WHITE)));
    }

    private void addCell(Table t, String text) {
        t.addCell(new Cell()
                .add(new Paragraph(text).setFontSize(9)));
    }

    private void addSubtotalCell(Table t, String text) {
        t.addCell(new Cell()
                .setBackgroundColor(GREEN_LIGHT)
                .add(new Paragraph(text).setBold().setFontSize(9)));
    }

    private void addGrandCell(Table t, String text) {
        t.addCell(new Cell()
                .setBackgroundColor(GREEN_DARK)
                .add(new Paragraph(text)
                        .setBold()
                        .setFontSize(10)
                        .setFontColor(ColorConstants.WHITE)));
    }
}