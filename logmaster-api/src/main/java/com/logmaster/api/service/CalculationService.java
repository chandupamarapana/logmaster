package com.logmaster.api.service;

import com.logmaster.api.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalculationService {

    private static final BigDecimal DIVISOR = new BigDecimal("2304");

    public BigDecimal computeCubicFeet(BigDecimal circumference, double lengthFt) {
        BigDecimal l = BigDecimal.valueOf(lengthFt);
        return circumference
                .multiply(circumference)
                .multiply(l)
                .divide(DIVISOR, 4, RoundingMode.HALF_UP);
    }

    public Category assignCategory(BigDecimal circumference, int lowMax, int midMax) {
        double c = circumference.doubleValue();
        if (c < lowMax) return Category.LOW;
        if (c <= midMax) return Category.MID;
        return Category.HIGH;
    }
    public void enrichLogEntry(LogEntry entry, int lowMax, int midMax) {
        double lengthFt = entry.getLogType().getLengthFt();
        entry.setLengthFt(BigDecimal.valueOf(lengthFt));
        entry.setCubicFeet(computeCubicFeet(entry.getCircumference(), lengthFt));
        entry.setCategory(assignCategory(entry.getCircumference(), lowMax, midMax));
    }
    // ── Private helper classes ─────────────────────────────────

    private static class TypeAccumulator {
        int totalLogs = 0;
        BigDecimal totalCft = BigDecimal.ZERO;
        BigDecimal totalPrice = BigDecimal.ZERO;
        Map<Category, CatAccumulator> cats = new EnumMap<>(Category.class);
    }

    private static class CatAccumulator {
        int logCount = 0;
        BigDecimal totalCft = BigDecimal.ZERO;
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal pricePerCft = null;
    }
    public Map<String, Object> buildSummary(Delivery delivery) {
        List<LogEntry> entries = delivery.getLogEntries();
        List<PriceConfig> prices = delivery.getPriceConfigs();

        int lowMax = delivery.getCategoryLowMax();
        int midMax = delivery.getCategoryMidMax();

        // Build price lookup: LogType -> Category -> price_per_cft
        Map<LogType, Map<Category, BigDecimal>> priceMap = new EnumMap<>(LogType.class);
        for (PriceConfig pc : prices) {
            priceMap
                    .computeIfAbsent(pc.getLogType(), k -> new EnumMap<>(Category.class))
                    .put(pc.getCategory(), pc.getPricePerCft());
        }

        // Initialise accumulators for each type
        Map<LogType, TypeAccumulator> accumulators = new EnumMap<>(LogType.class);
        for (LogType type : LogType.values()) {
            TypeAccumulator typeAcc = new TypeAccumulator();
            for (Category cat : Category.values()) {
                CatAccumulator catAcc = new CatAccumulator();
                catAcc.pricePerCft = priceMap
                        .getOrDefault(type, Map.of())
                        .get(cat);
                typeAcc.cats.put(cat, catAcc);
            }
            accumulators.put(type, typeAcc);
        }

        // Aggregate all log entries into accumulators
        for (LogEntry entry : entries) {
            TypeAccumulator typeAcc = accumulators.get(entry.getLogType());
            CatAccumulator catAcc = typeAcc.cats.get(entry.getCategory());

            BigDecimal cft = entry.getCubicFeet() != null
                    ? entry.getCubicFeet()
                    : BigDecimal.ZERO;

            typeAcc.totalLogs++;
            typeAcc.totalCft = typeAcc.totalCft.add(cft);

            catAcc.logCount++;
            catAcc.totalCft = catAcc.totalCft.add(cft);

            if (catAcc.pricePerCft != null) {
                BigDecimal linePrice = cft.multiply(catAcc.pricePerCft)
                        .setScale(2, RoundingMode.HALF_UP);
                catAcc.totalPrice = catAcc.totalPrice.add(linePrice);
                typeAcc.totalPrice = typeAcc.totalPrice.add(linePrice);
            }
        }

        // Grand totals
        int grandTotalLogs = 0;
        BigDecimal grandTotalCft = BigDecimal.ZERO;
        BigDecimal grandTotalPrice = BigDecimal.ZERO;

        for (TypeAccumulator acc : accumulators.values()) {
            grandTotalLogs += acc.totalLogs;
            grandTotalCft = grandTotalCft.add(acc.totalCft);
            grandTotalPrice = grandTotalPrice.add(acc.totalPrice);
        }

        // Build result map
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deliveryId", delivery.getId());
        result.put("supplierName", delivery.getSupplier().getName());
        result.put("deliveryDate", delivery.getDeliveryDate());
        result.put("categoryThresholds", Map.of(
                "lowMax", lowMax,
                "midMax", midMax
        ));
        result.put("accumulators", accumulators);
        result.put("grandTotalLogs", grandTotalLogs);
        result.put("grandTotalCft", grandTotalCft.setScale(4, RoundingMode.HALF_UP));
        result.put("grandTotalPrice", grandTotalPrice.setScale(2, RoundingMode.HALF_UP));

        return result;
    }
}
