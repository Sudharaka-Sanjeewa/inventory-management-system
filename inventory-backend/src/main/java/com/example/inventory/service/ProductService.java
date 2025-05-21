package com.example.inventory.service;

import com.example.inventory.entity.Category;
import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.Supplier;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.CategoryRepository;
import com.example.inventory.repository.SupplierRepository;
import com.example.inventory.repository.InventoryRepository; // Needed for deletion check
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
    private InventoryRepository inventoryRepository; // To check for existing inventory on product deletion

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public Product createProduct(Product product) {
        if (productRepository.existsBySku(product.getSku())) {
            throw new IllegalArgumentException("Product with SKU '" + product.getSku() + "' already exists.");
        }

        // Validate and fetch Category
        Category category = categoryRepository.findById(product.getCategory().getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Category ID provided."));

        // Validate and fetch Supplier
        Supplier supplier = supplierRepository.findById(product.getSupplier().getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Supplier ID provided."));

        product.setCategory(category);
        product.setSupplier(supplier);

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product updatedProduct) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        // Update fields that can be changed
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setSku(updatedProduct.getSku()); // Consider adding unique check if SKU is updated
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPurchasePrice(updatedProduct.getPurchasePrice());
        existingProduct.setSellingPrice(updatedProduct.getSellingPrice());

        // Handle Category update
        if (updatedProduct.getCategory() != null && updatedProduct.getCategory().getCategoryId() != null) {
            Category category = categoryRepository.findById(updatedProduct.getCategory().getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Category ID provided."));
            existingProduct.setCategory(category);
        }

        // Handle Supplier update
        if (updatedProduct.getSupplier() != null && updatedProduct.getSupplier().getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(updatedProduct.getSupplier().getSupplierId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Supplier ID provided."));
            existingProduct.setSupplier(supplier);
        }

        return productRepository.save(existingProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with ID: " + id);
        }

        // Check for associated Inventory before deleting
        Optional<Inventory> inventoryOptional = inventoryRepository.findByProduct_ProductId(id);
        if (inventoryOptional.isPresent()) {
            throw new IllegalStateException("Cannot delete product with existing inventory record. Delete inventory first.");
        }

        productRepository.deleteById(id);
    }
}