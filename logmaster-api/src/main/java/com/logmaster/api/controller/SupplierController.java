package com.logmaster.api.controller;


import com.logmaster.api.model.Supplier;
import com.logmaster.api.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public ResponseEntity<List<Supplier>> getAll() {
        return ResponseEntity.ok(supplierService.getAll());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getById(id));
    }
    @GetMapping("/search")
    public ResponseEntity<List<Supplier>> search(@RequestParam String name) {
        return ResponseEntity.ok(supplierService.search(name));
    }
    @PostMapping
    public ResponseEntity<Supplier> create(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        return ResponseEntity.status(201).body(supplierService.create(name));
    }

}
