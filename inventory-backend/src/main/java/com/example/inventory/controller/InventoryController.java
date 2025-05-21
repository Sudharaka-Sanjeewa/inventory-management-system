package com.example.inventory.controller;

import com.example.inventory.entity.Inventory;
import com.example.inventory.service.InventoryService; // Import the new Service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService; // Inject the Service

    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        List<Inventory> inventoryList = inventoryService.getAllInventory();
        return new ResponseEntity<>(inventoryList, HttpStatus.OK);
    }

    @GetMapping("/{productId}") // Endpoint to get inventory by Product ID
    public ResponseEntity<Inventory> getInventoryByProductId(@PathVariable Long productId) {
        return inventoryService.getInventoryByProductId(productId)
                .map(inventory -> new ResponseEntity<>(inventory, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Inventory> createInventory(@RequestBody Inventory inventory) {
        try {
            Inventory savedInventory = inventoryService.createInventory(inventory);
            return new ResponseEntity<>(savedInventory, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Product ID missing, not found, or inventory exists
        }
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Inventory> updateInventory(@PathVariable Long productId, @RequestBody Inventory updatedInventory) {
        try {
            Inventory savedInventory = inventoryService.updateInventory(productId, updatedInventory);
            return new ResponseEntity<>(savedInventory, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Inventory not found for product ID
        }
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Inventory>> getLowStockProducts() {
        List<Inventory> lowStockProducts = inventoryService.getLowStockProducts();
        return new ResponseEntity<>(lowStockProducts, HttpStatus.OK);
    }

    @DeleteMapping("/{id}") // Endpoint to delete inventory by its own inventoryId
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        try {
            inventoryService.deleteInventory(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Inventory not found
        }
    }
}