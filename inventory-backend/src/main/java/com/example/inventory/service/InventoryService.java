package com.example.inventory.service;

import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.Product;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.exception.DuplicateResourceException;
import com.example.inventory.exception.InvalidOperationException;
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
    private ProductRepository productRepository;

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public Inventory getInventoryByProductId(Long productId) { // Changed return type to Inventory and removed Optional
        return inventoryRepository.findByProduct_ProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product ID: " + productId));
    }

    public Inventory getInventoryById(Long id) { // Added this helper for update/delete by inventory ID if needed
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));
    }


    @Transactional
    public Inventory createInventory(Inventory inventory) {
        if (inventory.getProduct() == null || inventory.getProduct().getProductId() == null) {
            throw new InvalidOperationException("Product ID is required for inventory creation.");
        }

        Long productId = inventory.getProduct().getProductId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (inventoryRepository.findByProduct_ProductId(productId).isPresent()) {
            throw new DuplicateResourceException("Inventory record already exists for product ID: " + productId);
        }

        Inventory newInventory = new Inventory();
        newInventory.setProduct(product); // Link the fetched Product entity
        newInventory.setQuantityInStock(inventory.getQuantityInStock());
        newInventory.setLowStockThreshold(inventory.getLowStockThreshold());

        return inventoryRepository.save(newInventory);
    }

    @Transactional
    public Inventory updateInventory(Long productId, Inventory updatedInventory) {
        // Find inventory by Product ID, as this is how it's typically fetched/updated in this context
        Inventory existingInventory = inventoryRepository.findByProduct_ProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product ID: " + productId));

        // Update fields
        existingInventory.setQuantityInStock(updatedInventory.getQuantityInStock());
        existingInventory.setLowStockThreshold(updatedInventory.getLowStockThreshold());

        return inventoryRepository.save(existingInventory);
    }

    public List<Inventory> getLowStockProducts() {
        return inventoryRepository.findLowStockProducts();
    }

    @Transactional
    public void deleteInventory(Long id) { // This deletes by inventory ID, not product ID
        if (!inventoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Inventory not found with ID: " + id);
        }
        inventoryRepository.deleteById(id);
    }
}