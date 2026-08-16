package com.logmaster.api.service;

import com.logmaster.api.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class CalculationService {

    private static final BigDecimal DIVISOR = new BigDecimal("2304");

    // ── Public Methods ────────────────────────────────────────────────────────

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

    public Map<String, Object> buildSummary(Delivery delivery) {
        List<LogEntry> entries = delivery.getLogEntries();
        Collection<PriceConfig> prices = delivery.getPriceConfigs();
        int lowMax = delivery.getCategoryLowMax();
        int midMax = delivery.getCategoryMidMax();

        Map<LogType, Map<Category, BigDecimal>> priceMap = buildPriceMap(prices);
        Map<LogType, TypeAccumulator> accumulators = buildAccumulators(priceMap);
        aggregateEntries(entries, accumulators);

        return buildResultMap(delivery, accumulators, lowMax, midMax);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private Map<LogType, Map<Category, BigDecimal>> buildPriceMap(Collection<PriceConfig> prices) {
        Map<LogType, Map<Category, BigDecimal>> priceMap = new EnumMap<>(LogType.class);
        for (PriceConfig pc : prices) {
            priceMap
                    .computeIfAbsent(pc.getLogType(), k -> new EnumMap<>(Category.class))
                    .put(pc.getCategory(), pc.getPricePerCft());
        }
        return priceMap;
    }

    private Map<LogType, TypeAccumulator> buildAccumulators(
            Map<LogType, Map<Category, BigDecimal>> priceMap) {
        Map<LogType, TypeAccumulator> accumulators = new EnumMap<>(LogType.class);
        for (LogType type : LogType.values()) {
            TypeAccumulator typeAcc = new TypeAccumulator();
            for (Category cat : Category.values()) {
                CatAccumulator catAcc = new CatAccumulator();
                catAcc.pricePerCft = priceMap.getOrDefault(type, Map.of()).get(cat);
                typeAcc.cats.put(cat, catAcc);
            }
            accumulators.put(type, typeAcc);
        }
        return accumulators;
    }

    private void aggregateEntries(List<LogEntry> entries,
                                  Map<LogType, TypeAccumulator> accumulators) {
        for (LogEntry entry : entries) {
            TypeAccumulator typeAcc = accumulators.get(entry.getLogType());
            CatAccumulator catAcc = typeAcc.cats.get(entry.getCategory());
            BigDecimal cft = entry.getCubicFeet() != null
                    ? entry.getCubicFeet() : BigDecimal.ZERO;

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
    }

    private Map<String, Object> buildResultMap(Delivery delivery,
                                               Map<LogType, TypeAccumulator> accumulators,
                                               int lowMax, int midMax) {
        int grandTotalLogs = 0;
        BigDecimal grandTotalCft = BigDecimal.ZERO;
        BigDecimal grandTotalPrice = BigDecimal.ZERO;

        Map<String, Object> byType = new LinkedHashMap<>();
        for (LogType type : LogType.values()) {
            TypeAccumulator acc = accumulators.get(type);
            if (acc.totalLogs == 0) continue;

            grandTotalLogs += acc.totalLogs;
            grandTotalCft = grandTotalCft.add(acc.totalCft);
            grandTotalPrice = grandTotalPrice.add(acc.totalPrice);

            Map<String, Object> categories = new LinkedHashMap<>();
            for (Category cat : Category.values()) {
                CatAccumulator catAcc = acc.cats.get(cat);
                if (catAcc.logCount == 0) continue;
                Map<String, Object> catMap = new LinkedHashMap<>();
                catMap.put("logCount", catAcc.logCount);
                catMap.put("totalCft", catAcc.totalCft.setScale(2, RoundingMode.HALF_UP));
                catMap.put("pricePerCft", catAcc.pricePerCft);
                catMap.put("totalPrice", catAcc.totalPrice.setScale(2, RoundingMode.HALF_UP));
                categories.put(cat.name(), catMap);
            }

            Map<String, Object> typeMap = new LinkedHashMap<>();
            typeMap.put("totalLogs", acc.totalLogs);
            typeMap.put("totalCft", acc.totalCft.setScale(2, RoundingMode.HALF_UP));
            typeMap.put("totalPrice", acc.totalPrice.setScale(2, RoundingMode.HALF_UP));
            typeMap.put("categories", categories);
            byType.put(type.name(), typeMap);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deliveryId", delivery.getId());
        result.put("supplierName", delivery.getSupplier().getName());
        result.put("deliveryDate", delivery.getDeliveryDate());
        result.put("categoryThresholds", Map.of("lowMax", lowMax, "midMax", midMax));
        result.put("byType", byType);
        result.put("grandTotalLogs", grandTotalLogs);
        result.put("grandTotalCft", grandTotalCft.setScale(2, RoundingMode.HALF_UP));
        result.put("grandTotalPrice", grandTotalPrice.setScale(2, RoundingMode.HALF_UP));
        return result;
    }

    // ── Inner Classes ─────────────────────────────────────────────────────────

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
}