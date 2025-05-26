package com.example.inventory.repository;

import com.example.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // Added for the new method

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySku(String sku);

    // Add custom query methods if needed later
    List<Product> findBySupplier_SupplierId(Long supplierId); // Added for SupplierService deletion check
    List<Product> findByCategory_CategoryId(Long categoryId); //for CategoryService deletion check
}