package com.logmaster.api.repo;

import com.logmaster.api.model.Delivery;
import com.logmaster.api.model.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    List<Delivery> findBySupplierId(Long supplierId);
    List<Delivery> findByStatus(DeliveryStatus status);

    @Query("""
        SELECT d FROM Delivery d
        LEFT JOIN FETCH d.supplier
        LEFT JOIN FETCH d.logEntries
        LEFT JOIN FETCH d.priceConfigs
        WHERE d.id = :id
    """)
    Optional<Delivery> findByIdWithDetails(Long id);
}

