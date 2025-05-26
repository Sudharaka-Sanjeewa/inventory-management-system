package com.example.inventory.service;

import com.example.inventory.entity.Category;
import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.Supplier;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.CategoryRepository;
import com.example.inventory.repository.SupplierRepository;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.exception.DuplicateResourceException;
import com.example.inventory.exception.InvalidOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private InventoryRepository inventoryRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) { // Changed return type to Product and removed Optional
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    @Transactional
    public Product createProduct(Product product) {
        if (productRepository.existsBySku(product.getSku())) {
            throw new DuplicateResourceException("Product with SKU '" + product.getSku() + "' already exists.");
        }

        // Validate Category and Supplier IDs
        if (product.getCategory() == null || product.getCategory().getCategoryId() == null) {
            throw new InvalidOperationException("Category ID is required for product creation.");
        }
        Category category = categoryRepository.findById(product.getCategory().getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + product.getCategory().getCategoryId()));
        product.setCategory(category); // Set the managed entity

        if (product.getSupplier() == null || product.getSupplier().getSupplierId() == null) {
            throw new InvalidOperationException("Supplier ID is required for product creation.");
        }
        Supplier supplier = supplierRepository.findById(product.getSupplier().getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + product.getSupplier().getSupplierId()));
        product.setSupplier(supplier); // Set the managed entity

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product updatedProduct) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        // Check for duplicate SKU during update, but allow if it's the existing product's SKU
        if (updatedProduct.getSku() != null && !updatedProduct.getSku().equals(existingProduct.getSku()) &&
                productRepository.existsBySku(updatedProduct.getSku())) {
            throw new DuplicateResourceException("Product with SKU '" + updatedProduct.getSku() + "' already exists.");
        }

        // Update fields if provided in updatedProduct
        Optional.ofNullable(updatedProduct.getName()).ifPresent(existingProduct::setName);
        Optional.ofNullable(updatedProduct.getSku()).ifPresent(existingProduct::setSku);
        Optional.ofNullable(updatedProduct.getDescription()).ifPresent(existingProduct::setDescription);
        Optional.ofNullable(updatedProduct.getPurchasePrice()).ifPresent(existingProduct::setPurchasePrice);
        Optional.ofNullable(updatedProduct.getSellingPrice()).ifPresent(existingProduct::setSellingPrice);


        // Handle Category update
        if (updatedProduct.getCategory() != null && updatedProduct.getCategory().getCategoryId() != null) {
            Category category = categoryRepository.findById(updatedProduct.getCategory().getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + updatedProduct.getCategory().getCategoryId()));
            existingProduct.setCategory(category);
        }

        // Handle Supplier update
        if (updatedProduct.getSupplier() != null && updatedProduct.getSupplier().getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(updatedProduct.getSupplier().getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + updatedProduct.getSupplier().getSupplierId()));
            existingProduct.setSupplier(supplier);
        }

        return productRepository.save(existingProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        // First, check if the product exists
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with ID: " + id);
        }

        // Check for associated Inventory before deleting
        if (inventoryRepository.findByProduct_ProductId(id).isPresent()) {
            throw new InvalidOperationException("Cannot delete product with existing inventory record. Delete inventory first.");
        }

        productRepository.deleteById(id);
    }
}