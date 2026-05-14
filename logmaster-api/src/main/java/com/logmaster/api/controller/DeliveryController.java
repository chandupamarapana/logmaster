package com.logmaster.api.controller;

import com.logmaster.api.model.*;
import com.logmaster.api.service.DeliveryService;
import com.logmaster.api.service.LogEntryService;
import com.logmaster.api.service.PriceService;
import lombok.RequiredArgsConstructor;
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
    @PostMapping
    public ResponseEntity<Delivery> create(@RequestBody Map<String, Object> body) {
        Long supplierId = Long.valueOf(body.get("supplierId").toString());
        LocalDate deliveryDate = LocalDate.parse(body.get("deliveryDate").toString());
        Integer lowMax = body.containsKey("categoryLowMax") ?
                Integer.valueOf(body.get("categoryLowMax").toString()) : null;
        Integer midMax = body.containsKey("categoryMidMax") ?
                Integer.valueOf(body.get("categoryMidMax").toString()) : null;
        String notes = body.containsKey("notes") ?
                body.get("notes").toString() : null;

        return ResponseEntity.status(201)
                .body(deliveryService.create(supplierId, deliveryDate, lowMax, midMax, notes));
    }
    @GetMapping
    public ResponseEntity<List<Delivery>> getAll(
            @RequestParam(required = false) Long supplierId) {
        if (supplierId != null) {
            return ResponseEntity.ok(deliveryService.getBySupplierId(supplierId));
        }
        return ResponseEntity.ok(deliveryService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Delivery> getById(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getById(id));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<Map<String, Object>> getSummary(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getSummary(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Delivery> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        DeliveryStatus status = DeliveryStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(deliveryService.updateStatus(id, status));
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



}
