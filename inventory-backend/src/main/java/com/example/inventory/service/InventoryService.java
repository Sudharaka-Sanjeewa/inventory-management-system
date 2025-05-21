package com.example.inventory.service;

import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.Product;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private ProductRepository productRepository; // Needed to link Inventory to Product

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public Optional<Inventory> getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProduct_ProductId(productId);
    }

    @Transactional
    public Inventory createInventory(Inventory inventory) {
        if (inventory.getProduct() == null || inventory.getProduct().getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required for inventory creation.");
        }

        Long productId = inventory.getProduct().getProductId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        if (inventoryRepository.findByProduct_ProductId(productId).isPresent()) {
            throw new IllegalArgumentException("Inventory record already exists for product ID: " + productId);
        }

        Inventory newInventory = new Inventory();
        newInventory.setProduct(product);
        newInventory.setQuantityInStock(inventory.getQuantityInStock());
        newInventory.setLowStockThreshold(inventory.getLowStockThreshold());

        return inventoryRepository.save(newInventory);
    }

    @Transactional
    public Inventory updateInventory(Long productId, Inventory updatedInventory) {
        Inventory existingInventory = inventoryRepository.findByProduct_ProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for product ID: " + productId));

        existingInventory.setQuantityInStock(updatedInventory.getQuantityInStock());
        existingInventory.setLowStockThreshold(updatedInventory.getLowStockThreshold());

        return inventoryRepository.save(existingInventory);
    }

    public List<Inventory> getLowStockProducts() {
        return inventoryRepository.findLowStockProducts();
    }

    @Transactional
    public void deleteInventory(Long id) {
        if (!inventoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Inventory not found with ID: " + id);
        }
        inventoryRepository.deleteById(id);
    }
}