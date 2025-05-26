package com.example.inventory.service;

import com.example.inventory.entity.Product;
import com.example.inventory.entity.Supplier;
import com.example.inventory.repository.SupplierRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.exception.DuplicateResourceException;
import com.example.inventory.exception.InvalidOperationException; // For deletion check
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private ProductRepository productRepository; // To check for existing products on supplier deletion

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public Supplier getSupplierById(Long id) { // Changed return type to Supplier and removed Optional
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id));
    }

    @Transactional
    public Supplier createSupplier(Supplier supplier) {
        if (supplierRepository.existsByName(supplier.getName())) {
            throw new DuplicateResourceException("Supplier with name '" + supplier.getName() + "' already exists.");
        }
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier updateSupplier(Long id, Supplier updatedSupplier) {
        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id));

        // Check for duplicate name during update, but allow if it's the existing supplier's name
        if (supplierRepository.existsByName(updatedSupplier.getName()) &&
                !existingSupplier.getName().equals(updatedSupplier.getName())) {
            throw new DuplicateResourceException("Supplier with name '" + updatedSupplier.getName() + "' already exists.");
        }

        existingSupplier.setName(updatedSupplier.getName());
        existingSupplier.setContactInfo(updatedSupplier.getContactInfo());

        return supplierRepository.save(existingSupplier);
    }

    @Transactional
    public void deleteSupplier(Long id) {
        // First, check if the supplier exists
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supplier not found with ID: " + id);
        }

        // Check for associated Products before deleting
        List<Product> associatedProducts = productRepository.findBySupplier_SupplierId(id);
        if (!associatedProducts.isEmpty()) {
            throw new InvalidOperationException("Cannot delete supplier with existing products. Delete associated products first.");
        }

        supplierRepository.deleteById(id);
    }
}