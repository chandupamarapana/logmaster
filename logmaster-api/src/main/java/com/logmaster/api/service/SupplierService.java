package com.logmaster.api.service;

import com.logmaster.api.exception.ResourceNotFoundException;
import com.logmaster.api.model.Supplier;
import com.logmaster.api.repo.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public List<Supplier> getAll(){
        return supplierRepository.findAll();
    }

    public Supplier getById(Long id){
        return supplierRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("supplier not found: "+ id));
    }
    public List<Supplier> search(String name) {
        return supplierRepository.findByNameContainingIgnoreCaseOrderByNameAsc(name);
    }
    public Supplier create(String name) {
        Supplier supplier = Supplier.builder()
                .name(name.trim())
                .build();
        return supplierRepository.save(supplier);
    }
}
