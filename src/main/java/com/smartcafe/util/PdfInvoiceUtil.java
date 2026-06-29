package com.smartcafe.util;

import com.smartcafe.model.Order;
import com.smartcafe.model.OrderItem;
import com.smartcafe.model.Payment;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.printing.PDFPageable;

import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Generates receipt-style PDF invoices using Apache PDFBox 2.x.
 *
 * Page format: 226 pt wide (80 mm) × auto-height — standard thermal receipt width.
 */
public final class PdfInvoiceUtil {

    private static final float PAGE_W      = 226f;   // 80mm in points
    private static final float MARGIN      = 14f;
    private static final float LINE_H      = 14f;
    private static final float FONT_NORMAL = 9f;
    private static final float FONT_SMALL  = 8f;
    private static final float FONT_LARGE  = 12f;
    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("MMM d, yyyy  h:mm a");

    private PdfInvoiceUtil() {}

    // ── Public API ────────────────────────────────────────────────────────────

    /** Saves the invoice PDF to a file and returns it. */
    public static File savePdf(Order order, File outputFile) throws IOException {
        try (PDDocument doc = buildDocument(order)) {
            doc.save(outputFile);
        }
        return outputFile;
    }

    /** Sends the invoice directly to the default printer. */
    public static void print(Order order) throws Exception {
        try (PDDocument doc = buildDocument(order)) {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPageable(new PDFPageable(doc));
            if (job.printDialog()) job.print();
        }
    }

    // ── Document construction ─────────────────────────────────────────────────

    private static PDDocument buildDocument(Order order) throws IOException {
        // Calculate required height
        int itemCount   = order.getItems() == null ? 0 : order.getItems().size();
        float pageH     = 40 + LINE_H * (22 + itemCount * 2) + 120; // header + items + footer + QR
        pageH           = Math.max(pageH, 500f);

        PDDocument doc  = new PDDocument();
        PDRectangle rect = new PDRectangle(PAGE_W, pageH);
        PDPage page     = new PDPage(rect);
        doc.addPage(page);

        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            float y = pageH - MARGIN;

            y = drawHeader(cs, y);
            y = drawDivider(cs, y);
            y = drawOrderInfo(cs, order, y);
            y = drawDivider(cs, y);
            y = drawItems(cs, order, y);
            y = drawDivider(cs, y);
            y = drawTotals(cs, order, y);
            y = drawDivider(cs, y);
            y = drawPayment(cs, order, y);
            y = drawDivider(cs, y);
            y = drawQr(doc, page, cs, order, y);
            drawFooter(cs, y - LINE_H);
        }
        return doc;
    }

    // ── Section writers ───────────────────────────────────────────────────────

    private static float drawHeader(PDPageContentStream cs, float y) throws IOException {
        y = drawCentred(cs, "SMART CAFE", PDType1Font.HELVETICA_BOLD, FONT_LARGE, y);
        y = drawCentred(cs, "Smart AI-Based Cafe Management System", PDType1Font.HELVETICA, FONT_SMALL, y);
        y = drawCentred(cs, "123 Coffee St., Manila, Philippines", PDType1Font.HELVETICA, FONT_SMALL, y);
        y -= 4;
        y = drawCentred(cs, "OFFICIAL RECEIPT", PDType1Font.HELVETICA_BOLD, FONT_NORMAL, y);
        return y;
    }

    private static float drawOrderInfo(PDPageContentStream cs, Order order, float y) throws IOException {
        y = drawRow(cs, "Order #:", order.getOrderNumber(), y);
        if (order.getCreatedAt() != null)
            y = drawRow(cs, "Date:", order.getCreatedAt().format(DT_FMT), y);
        y = drawRow(cs, "Cashier:", nvl(order.getCashierName()), y);
        y = drawRow(cs, "Type:", nvl(order.getOrderType()).replace("_", " "), y);
        if (order.getTableNumber() != null)
            y = drawRow(cs, "Table:", order.getTableNumber(), y);
        if (order.getCustomerName() != null)
            y = drawRow(cs, "Customer:", order.getCustomerName(), y);
        return y;
    }

    private static float drawItems(PDPageContentStream cs, Order order, float y) throws IOException {
        // Header row
        y = drawItemRow(cs, "Item", "Qty", "Price", "Total", PDType1Font.HELVETICA_BOLD, y);
        y -= 2;
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                String name = truncate(item.getProductName(), 18);
                y = drawItemRow(cs, name,
                        String.valueOf(item.getQuantity()),
                        String.format("%.2f", item.getUnitPrice()),
                        String.format("%.2f", item.getSubtotal()),
                        PDType1Font.HELVETICA, y);
            }
        }
        return y;
    }

    private static float drawTotals(PDPageContentStream cs, Order order, float y) throws IOException {
        y = drawRow(cs, "Subtotal:", String.format("₱ %.2f", order.getSubtotal()), y);
        if (order.getDiscount() > 0)
            y = drawRow(cs, "Discount:", String.format("- ₱ %.2f", order.getDiscount()), y);
        y = drawRow(cs, "Tax (12%):", String.format("₱ %.2f", order.getTax()), y);
        y -= 2;
        y = drawRow(cs, "TOTAL DUE:", String.format("₱ %.2f", order.getTotal()),
                PDType1Font.HELVETICA_BOLD, y);
        return y;
    }

    private static float drawPayment(PDPageContentStream cs, Order order, float y) throws IOException {
        Payment p = order.getPayment();
        if (p == null) return y;
        y = drawRow(cs, "Payment Method:", nvl(p.getPaymentMethod()), y);
        y = drawRow(cs, "Amount Paid:", String.format("₱ %.2f", p.getAmountPaid()), y);
        if (p.getChangeAmount() > 0)
            y = drawRow(cs, "Change:", String.format("₱ %.2f", p.getChangeAmount()), y);
        if (p.getTransactionRef() != null)
            y = drawRow(cs, "Ref #:", p.getTransactionRef(), y);
        return y;
    }

    private static float drawQr(PDDocument doc, PDPage page,
                                 PDPageContentStream cs, Order order, float y) throws IOException {
        String qrContent = "Order:" + order.getOrderNumber()
                + "|Total:₱" + String.format("%.2f", order.getTotal());
        BufferedImage qrImg = QrCodeUtil.generate(qrContent, 100);
        if (qrImg != null) {
            PDImageXObject pdImg = LosslessFactory.createFromImage(doc, qrImg);
            float imgW = 80f;
            float imgX = (PAGE_W - imgW) / 2f;
            y -= 4;
            cs.drawImage(pdImg, imgX, y - imgW, imgW, imgW);
            y -= imgW + 4;
        }
        return y;
    }

    private static void drawFooter(PDPageContentStream cs, float y) throws IOException {
        drawCentred(cs, "Thank you for visiting Smart Cafe!", PDType1Font.HELVETICA_OBLIQUE, FONT_SMALL, y);
    }

    // ── Low-level drawing helpers ─────────────────────────────────────────────

    private static float drawCentred(PDPageContentStream cs, String text,
                                     PDType1Font font, float size, float y) throws IOException {
        float textW = font.getStringWidth(text) / 1000 * size;
        float x     = (PAGE_W - textW) / 2f;
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        return y - LINE_H;
    }

    private static float drawRow(PDPageContentStream cs, String label, String value, float y)
            throws IOException {
        return drawRow(cs, label, value, PDType1Font.HELVETICA, y);
    }

    private static float drawRow(PDPageContentStream cs, String label, String value,
                                 PDType1Font font, float y) throws IOException {
        cs.beginText();
        cs.setFont(font, FONT_NORMAL);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(label);
        cs.endText();

        float valW = font.getStringWidth(value) / 1000 * FONT_NORMAL;
        cs.beginText();
        cs.setFont(font, FONT_NORMAL);
        cs.newLineAtOffset(PAGE_W - MARGIN - valW, y);
        cs.showText(value);
        cs.endText();
        return y - LINE_H;
    }

    private static float drawItemRow(PDPageContentStream cs, String name, String qty,
                                     String price, String total, PDType1Font font, float y)
            throws IOException {
        cs.beginText();
        cs.setFont(font, FONT_SMALL);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(name);
        cs.endText();

        float col2 = MARGIN + 95;
        float col3 = MARGIN + 125;
        float col4 = PAGE_W - MARGIN - font.getStringWidth(total) / 1000 * FONT_SMALL;

        for (String[] pair : new String[][]{{qty, String.valueOf(col2)}, {price, String.valueOf(col3)}}) {
            cs.beginText();
            cs.setFont(font, FONT_SMALL);
            cs.newLineAtOffset(Float.parseFloat(pair[1]), y);
            cs.showText(pair[0]);
            cs.endText();
        }
        cs.beginText();
        cs.setFont(font, FONT_SMALL);
        cs.newLineAtOffset(col4, y);
        cs.showText(total);
        cs.endText();
        return y - LINE_H;
    }

    private static float drawDivider(PDPageContentStream cs, float y) throws IOException {
        y -= 3;
        cs.setLineWidth(0.5f);
        cs.moveTo(MARGIN, y);
        cs.lineTo(PAGE_W - MARGIN, y);
        cs.stroke();
        return y - 5;
    }

    private static String nvl(String s) { return s != null ? s : "—"; }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
