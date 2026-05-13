package com.logmaster.api.repo;

import com.logmaster.api.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findByNameContainingIgnoreCaseOrderByNameAsc (String name);
}
