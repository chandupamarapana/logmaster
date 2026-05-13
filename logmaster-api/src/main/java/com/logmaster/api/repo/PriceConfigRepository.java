package com.logmaster.api.repo;

import com.logmaster.api.model.Category;
import com.logmaster.api.model.LogType;
import com.logmaster.api.model.PriceConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriceConfigRepository extends JpaRepository<PriceConfig, Long> {
    List<PriceConfig> findByDeliveryId(Long deliveryId);

    Optional<PriceConfig> findByDeliveryIdAndLogTypeAndCategory(
            Long deliveryId, LogType logType, Category category);
}
