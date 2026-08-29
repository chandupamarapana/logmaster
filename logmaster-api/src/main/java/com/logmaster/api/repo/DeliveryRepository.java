package com.logmaster.api.repo;

import com.logmaster.api.model.Delivery;
import com.logmaster.api.model.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    List<Delivery> findByStatus(DeliveryStatus status);

    @Query("""
    SELECT d FROM Delivery d
    LEFT JOIN FETCH d.supplier
    WHERE d.id = :id
""")
    Optional<Delivery> findByIdWithSupplier(Long id);

    @Query("""
    SELECT d FROM Delivery d
    LEFT JOIN FETCH d.supplier
    WHERE d.supplier.id = :supplierId
    ORDER BY d.deliveryDate DESC
""")
    List<Delivery> findBySupplierIdWithSupplier(Long supplierId);

    @Query("""
    SELECT d FROM Delivery d
    LEFT JOIN FETCH d.supplier
    ORDER BY d.deliveryDate DESC
""")
    List<Delivery> findAllWithSupplier();
    @Query("""
    SELECT d FROM Delivery d
    LEFT JOIN FETCH d.supplier s
    WHERE s.company.id = :companyId
    ORDER BY d.deliveryDate DESC
""")
    List<Delivery> findAllByCompanyId(@Param("companyId") Long companyId);
}


