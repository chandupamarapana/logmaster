package com.logmaster.api.dto;

import com.logmaster.api.model.Delivery;
import com.logmaster.api.model.DeliveryStatus;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class DeliveryResponse {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private LocalDate deliveryDate;
    private int categoryLowMax;
    private int categoryMidMax;
    private DeliveryStatus status;
    private LocalDateTime createdAt;

    public DeliveryResponse(Delivery delivery) {
        this.id = delivery.getId();
        this.supplierId = delivery.getSupplier().getId();
        this.supplierName = delivery.getSupplier().getName();
        this.deliveryDate = delivery.getDeliveryDate();
        this.categoryLowMax = delivery.getCategoryLowMax();
        this.categoryMidMax = delivery.getCategoryMidMax();
        this.status = delivery.getStatus();
        this.createdAt = delivery.getCreatedAt();
    }
}