package com.logmaster.api.service;

import com.logmaster.api.exception.ResourceNotFoundException;
import com.logmaster.api.model.Delivery;
import com.logmaster.api.model.LogEntry;
import com.logmaster.api.model.LogType;
import com.logmaster.api.repo.DeliveryRepository;
import com.logmaster.api.repo.LogEntryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LogEntryService {

    private final LogEntryRepository logEntryRepository;
    private final DeliveryRepository deliveryRepository;
    private final CalculationService calculationService;

    @Transactional
    public LogEntry addEntry(Long deliveryId, LogType logType, BigDecimal circumference) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found: " + deliveryId));

        int order = logEntryRepository.countByDeliveryIdAndLogType(deliveryId, logType);

        LogEntry entry = LogEntry.builder()
                .delivery(delivery)
                .logType(logType)
                .circumference(circumference)
                .entryOrder(order)
                .build();

        calculationService.enrichLogEntry(entry,
                delivery.getCategoryLowMax(),
                delivery.getCategoryMidMax());

        return logEntryRepository.save(entry);
    }

    @Transactional
    public List<LogEntry> bulkAdd(Long deliveryId, List<LogEntry> entries) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

        Map<LogType, Integer> counters = new EnumMap<>(LogType.class);
        for (LogType type : LogType.values()) {
            counters.put(type, logEntryRepository
                    .countByDeliveryIdAndLogType(deliveryId, type));
        }

        for (LogEntry entry : entries) {
            int order = counters.get(entry.getLogType());
            counters.put(entry.getLogType(), order + 1);
            entry.setDelivery(delivery);
            entry.setEntryOrder(order);
            calculationService.enrichLogEntry(entry,
                    delivery.getCategoryLowMax(),
                    delivery.getCategoryMidMax());
        }

        return logEntryRepository.saveAll(entries);
    }

    public List<LogEntry> getByDelivery(Long deliveryId) {
        return logEntryRepository
                .findByDeliveryIdOrderByLogTypeAscEntryOrderAsc(deliveryId);
    }

    @Transactional
    public void delete(Long deliveryId, Long logId) {
        LogEntry entry = logEntryRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Log entry not found: " + logId));

        if (!entry.getDelivery().getId().equals(deliveryId)) {
            throw new RuntimeException("Log entry does not belong to this delivery");
        }

        logEntryRepository.delete(entry);
    }
}