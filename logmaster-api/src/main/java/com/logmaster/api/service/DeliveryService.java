package com.logmaster.api.service;

import com.logmaster.api.model.*;
import com.logmaster.api.repo.DeliveryRepository;
import com.logmaster.api.repo.LogEntryRepository;
import com.logmaster.api.repo.PriceConfigRepository;
import com.logmaster.api.repo.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final SupplierRepository supplierRepository;
    private final CalculationService calculationService;
    private final LogEntryRepository logEntryRepository;
    private final PriceConfigRepository priceConfigRepository;

    public Delivery create(Long supplierId, LocalDate deliveryDate,
                           Integer categoryLowMax, Integer categoryMidMax, String notes) {

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + supplierId));

        Delivery delivery = Delivery.builder()
                .supplier(supplier)
                .deliveryDate(deliveryDate)
                .categoryLowMax(categoryLowMax != null ? categoryLowMax : 23)
                .categoryMidMax(categoryMidMax != null ? categoryMidMax : 30)
                .notes(notes)
                .status(DeliveryStatus.DRAFT)
                .build();

        return deliveryRepository.save(delivery);
    }

    public List<Delivery> getAll() {
        return deliveryRepository.findAll();
    }

    public List<Delivery> getBySupplierId(Long supplierId) {
        return deliveryRepository.findBySupplierId(supplierId);
    }

    public Delivery getById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + id));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSummary(Long id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + id));

        // Load separately to avoid MultipleBagFetchException and duplicate rows
        List<LogEntry> logEntries = logEntryRepository
                .findByDeliveryIdOrderByLogTypeAscEntryOrderAsc(id);
        List<PriceConfig> priceConfigs = priceConfigRepository.findByDeliveryId(id);

        delivery.setLogEntries(logEntries);
        delivery.setPriceConfigs(new HashSet<>(priceConfigs));

        return calculationService.buildSummary(delivery);
    }

    @Transactional
    public Delivery updateStatus(Long id, DeliveryStatus status) {
        Delivery delivery = getById(id);
        delivery.setStatus(status);
        return deliveryRepository.save(delivery);
    }
}