package com.logmaster.api.service;

import com.logmaster.api.config.SecurityUtil;
import com.logmaster.api.exception.ResourceNotFoundException;
import com.logmaster.api.model.Company;
import com.logmaster.api.model.Supplier;
import com.logmaster.api.repo.CompanyRepository;
import com.logmaster.api.repo.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final SecurityUtil securityUtil;
    private final CompanyRepository companyRepository;

    public List<Supplier> getAll() {
        return supplierRepository.findAll();
    }

    public Supplier getById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("supplier not found: " + id));
    }

    public List<Supplier> search(String name) {
        return supplierRepository.findByNameContainingIgnoreCaseOrderByNameAsc(name);
    }

    public Supplier create(String name) {
        Long companyId = securityUtil.getCurrentCompanyId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        Supplier supplier = Supplier.builder()
                .name(name.trim())
                .company(company)
                .build();
        return supplierRepository.save(supplier);
    }
}
