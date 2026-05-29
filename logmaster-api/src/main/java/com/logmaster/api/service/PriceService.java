package com.logmaster.api.service;

import com.logmaster.api.exception.ResourceNotFoundException;
import com.logmaster.api.model.Delivery;
import com.logmaster.api.model.DeliveryStatus;
import com.logmaster.api.model.PriceConfig;
import com.logmaster.api.repo.DeliveryRepository;
import com.logmaster.api.repo.PriceConfigRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceService {

    private final PriceConfigRepository priceConfigRepository;
    private final DeliveryRepository deliveryRepository;

    @Transactional
    public List<PriceConfig> setPrices(Long deliveryId, List<PriceConfig> prices) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found: " + deliveryId));

        for (PriceConfig price : prices) {
            PriceConfig existing = priceConfigRepository
                    .findByDeliveryIdAndLogTypeAndCategory(
                            deliveryId,
                            price.getLogType(),
                            price.getCategory())
                    .orElse(null);

            if (existing != null) {
                existing.setPricePerCft(price.getPricePerCft());
                priceConfigRepository.save(existing);
            } else {
                price.setDelivery(delivery);
                priceConfigRepository.save(price);
            }
        }

        if (delivery.getStatus() == DeliveryStatus.DRAFT) {
            delivery.setStatus(DeliveryStatus.PRICED);
            deliveryRepository.save(delivery);
        }

        return priceConfigRepository.findByDeliveryId(deliveryId);
    }

    public List<PriceConfig> getPrices(Long deliveryId) {
        return priceConfigRepository.findByDeliveryId(deliveryId);
    }
}