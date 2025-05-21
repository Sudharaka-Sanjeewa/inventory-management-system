package com.example.inventory.service;

import com.example.inventory.entity.Product; // Needed for deletion check
import com.example.inventory.entity.Supplier;
import com.example.inventory.repository.SupplierRepository;
import com.example.inventory.repository.ProductRepository; // Needed for deletion check
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

    @Transactional
    public Supplier createSupplier(Supplier supplier) {
        if (supplierRepository.existsByName(supplier.getName())) {
            throw new IllegalArgumentException("Supplier with name '" + supplier.getName() + "' already exists.");
        }
        return supplierRepository.save(supplier);
    }

    public Optional<Supplier> getSupplierById(Long id) {
        return supplierRepository.findById(id);
    }

    @Transactional
    public Supplier updateSupplier(Long id, Supplier updatedSupplier) {
        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with ID: " + id));

        existingSupplier.setName(updatedSupplier.getName());
        existingSupplier.setContactInfo(updatedSupplier.getContactInfo());

        return supplierRepository.save(existingSupplier);
    }

    @Transactional
    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new IllegalArgumentException("Supplier not found with ID: " + id);
        }

        // Check for associated Products before deleting
        // Assuming ProductRepository has a method to find products by supplier ID
        // We need to add this method to ProductRepository first.
        // For now, let's assume `findBySupplier_SupplierId` is available or implement it.
        // If not, you'd add: `List<Product> findBySupplier_SupplierId(Long supplierId);` to ProductRepository.java
        List<Product> associatedProducts = productRepository.findBySupplier_SupplierId(id);
        if (!associatedProducts.isEmpty()) {
            throw new IllegalStateException("Cannot delete supplier with existing products. Delete products first.");
        }

        supplierRepository.deleteById(id);
    }
}