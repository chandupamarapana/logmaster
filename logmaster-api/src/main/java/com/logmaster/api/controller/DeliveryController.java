package com.logmaster.api.controller;

import com.logmaster.api.dto.DeliveryResponse;
import com.logmaster.api.model.*;
import com.logmaster.api.service.DeliveryService;
import com.logmaster.api.service.LogEntryService;
import com.logmaster.api.service.PdfService;
import com.logmaster.api.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService deliveryService;
    private final LogEntryService logEntryService;
    private final PriceService priceService;
    private final PdfService pdfService;
    @PostMapping
    public ResponseEntity<DeliveryResponse> create(@RequestBody Map<String, Object> body) {
        Long supplierId = Long.valueOf(body.get("supplierId").toString());
        LocalDate deliveryDate = LocalDate.parse(body.get("deliveryDate").toString());
        Integer lowMax = body.containsKey("categoryLowMax") ?
                Integer.valueOf(body.get("categoryLowMax").toString()) : null;
        Integer midMax = body.containsKey("categoryMidMax") ?
                Integer.valueOf(body.get("categoryMidMax").toString()) : null;
        String notes = body.containsKey("notes") ?
                body.get("notes").toString() : null;

        Delivery delivery = deliveryService.create(supplierId, deliveryDate, lowMax, midMax, notes);
        return ResponseEntity.status(201).body(new DeliveryResponse(delivery));
    }
    @GetMapping
    public ResponseEntity<List<DeliveryResponse>> getAll(
            @RequestParam(required = false) Long supplierId) {
        List<Delivery> deliveries = supplierId != null
                ? deliveryService.getBySupplierId(supplierId)
                : deliveryService.getAll();

        List<DeliveryResponse> response = deliveries.stream()
                .map(DeliveryResponse::new)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new DeliveryResponse(deliveryService.getById(id)));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<Map<String, Object>> getSummary(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getSummary(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DeliveryResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        DeliveryStatus status = DeliveryStatus.valueOf(body.get("status"));
        Delivery delivery = deliveryService.updateStatus(id, status);
        return ResponseEntity.ok(new DeliveryResponse(delivery));
    }
    // ── LOG ENTRIES ───────────────────────────────────────────

    @PostMapping("/{id}/logs")
    public ResponseEntity<LogEntry> addLog(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        LogType logType = LogType.valueOf(body.get("logType"));
        BigDecimal circumference = new BigDecimal(body.get("circumference"));
        return ResponseEntity.status(201)
                .body(logEntryService.addEntry(id, logType, circumference));
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<List<LogEntry>> getLogs(@PathVariable Long id) {
        return ResponseEntity.ok(logEntryService.getByDelivery(id));
    }

    @DeleteMapping("/{id}/logs/{logId}")
    public ResponseEntity<Void> deleteLog(
            @PathVariable Long id,
            @PathVariable Long logId) {
        logEntryService.delete(id, logId);
        return ResponseEntity.noContent().build();
    }
    // ── PRICES ────────────────────────────────────────────────

    @PutMapping("/{id}/prices")
    public ResponseEntity<List<PriceConfig>> setPrices(
            @PathVariable Long id,
            @RequestBody List<PriceConfig> prices) {
        return ResponseEntity.ok(priceService.setPrices(id, prices));
    }

    @GetMapping("/{id}/prices")
    public ResponseEntity<List<PriceConfig>> getPrices(@PathVariable Long id) {
        return ResponseEntity.ok(priceService.getPrices(id));
    }
    @GetMapping("/{id}/report")
    public ResponseEntity<byte[]> generateReport(@PathVariable Long id) {
        Map<String, Object> summary = deliveryService.getSummary(id);
        List<LogEntry> entries = logEntryService.getByDelivery(id);
        List<PriceConfig> prices = priceService.getPrices(id);

        // Build a simple delivery object with supplier name from summary
        Delivery delivery = deliveryService.getById(id);

        byte[] pdf = pdfService.generateReport(summary, delivery, entries);

        String filename = "LogMaster_" +
                summary.get("supplierName").toString().replace(" ", "_") +
                "_" + summary.get("deliveryDate").toString() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }



}
