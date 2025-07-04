package com.example.inventory.controller;

import com.example.inventory.dto.supplier.CreateSupplierRequest;
import com.example.inventory.dto.supplier.SupplierDTO;
import com.example.inventory.dto.supplier.UpdateSupplierRequest;
import com.example.inventory.entity.Supplier;
import com.example.inventory.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    // Helper method to convert Supplier entity to SupplierDTO
    private SupplierDTO convertToDto(Supplier supplier) {
        SupplierDTO dto = new SupplierDTO();
        dto.setSupplierId(supplier.getSupplierId());
        dto.setName(supplier.getName());
        dto.setContactInfo(supplier.getContactInfo());
        return dto;
    }

    // Helper method to convert CreateSupplierRequest to Supplier entity
    private Supplier convertToEntity(CreateSupplierRequest dto) {
        Supplier supplier = new Supplier();
        supplier.setName(dto.getName());
        supplier.setContactInfo(dto.getContactInfo());
        return supplier;
    }

    // Helper method to update Supplier entity from UpdateSupplierRequest
    private Supplier convertToEntity(UpdateSupplierRequest dto, Supplier existingSupplier) {
        if (dto.getName() != null) {
            existingSupplier.setName(dto.getName());
        }
        if (dto.getContactInfo() != null) {
            existingSupplier.setContactInfo(dto.getContactInfo());
        }
        return existingSupplier;
    }

    @GetMapping
    public ResponseEntity<List<SupplierDTO>> getAllSuppliers() {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        List<SupplierDTO> supplierDTOs = suppliers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(supplierDTOs, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierDTO> getSupplierById(@PathVariable Long id) {
        // Service will throw ResourceNotFoundException if not found
        Supplier supplier = supplierService.getSupplierById(id);
        return new ResponseEntity<>(convertToDto(supplier), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<SupplierDTO> createSupplier(@Valid @RequestBody CreateSupplierRequest createSupplierRequest) {
        // Service will throw DuplicateResourceException if name exists
        Supplier supplierToCreate = convertToEntity(createSupplierRequest);
        Supplier savedSupplier = supplierService.createSupplier(supplierToCreate);
        return new ResponseEntity<>(convertToDto(savedSupplier), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierDTO> updateSupplier(@PathVariable Long id, @Valid @RequestBody UpdateSupplierRequest updateSupplierRequest) {
        // Service will throw ResourceNotFoundException or DuplicateResourceException
        Supplier existingSupplier = new Supplier(); // Dummy entity
        Supplier updatedSupplierEntity = convertToEntity(updateSupplierRequest, existingSupplier);
        Supplier savedSupplier = supplierService.updateSupplier(id, updatedSupplierEntity);
        return new ResponseEntity<>(convertToDto(savedSupplier), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        // Service will throw ResourceNotFoundException or InvalidOperationException
        supplierService.deleteSupplier(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}