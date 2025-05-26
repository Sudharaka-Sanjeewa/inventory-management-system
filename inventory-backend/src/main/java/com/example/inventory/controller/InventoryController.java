package com.example.inventory.controller;

import com.example.inventory.dto.inventory.CreateInventoryRequest;
import com.example.inventory.dto.inventory.InventoryDTO;
import com.example.inventory.dto.inventory.UpdateInventoryRequest;
import com.example.inventory.dto.product.ProductDTO;
import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.Product;
import com.example.inventory.service.InventoryService;
import com.example.inventory.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductService productService;

    // Helper method to convert Product entity to ProductDTO for nesting in InventoryDTO
    private ProductDTO convertProductToDto(Product product) {
        if (product == null) {
            return null;
        }
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setSku(product.getSku());
        dto.setDescription(product.getDescription());
        return dto;
    }

    // Helper method to convert Inventory entity to InventoryDTO
    private InventoryDTO convertToDto(Inventory inventory) {
        InventoryDTO dto = new InventoryDTO();
        dto.setInventoryId(inventory.getInventoryId());
        dto.setProduct(convertProductToDto(inventory.getProduct()));
        dto.setQuantityInStock(inventory.getQuantityInStock());
        dto.setLowStockThreshold(inventory.getLowStockThreshold());
        dto.setCreatedAt(inventory.getCreatedAt());
        dto.setLastUpdated(inventory.getLastUpdated());
        return dto;
    }

    // Helper method to convert CreateInventoryRequest to Inventory entity for service layer
    private Inventory convertToEntity(CreateInventoryRequest dto) {
        Inventory inventory = new Inventory();
        Product product = new Product();
        product.setProductId(dto.getProductId());
        inventory.setProduct(product);
        inventory.setQuantityInStock(dto.getQuantityInStock());
        inventory.setLowStockThreshold(dto.getLowStockThreshold());
        return inventory;
    }

    // Helper method to update Inventory entity from UpdateInventoryRequest
    private Inventory convertToEntity(UpdateInventoryRequest dto, Inventory existingInventory) {
        if (dto.getQuantityInStock() != null) {
            existingInventory.setQuantityInStock(dto.getQuantityInStock());
        }
        if (dto.getLowStockThreshold() != null) {
            existingInventory.setLowStockThreshold(dto.getLowStockThreshold());
        }
        return existingInventory;
    }

    @GetMapping
    public ResponseEntity<List<InventoryDTO>> getAllInventory() {
        List<Inventory> inventoryList = inventoryService.getAllInventory();
        List<InventoryDTO> inventoryDTOs = inventoryList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(inventoryDTOs, HttpStatus.OK);
    }

    @GetMapping("/{productId}") // Endpoint to get inventory by Product ID
    public ResponseEntity<InventoryDTO> getInventoryByProductId(@PathVariable Long productId) {
        // Service will throw ResourceNotFoundException if not found
        Inventory inventory = inventoryService.getInventoryByProductId(productId);
        return new ResponseEntity<>(convertToDto(inventory), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<InventoryDTO> createInventory(@Valid @RequestBody CreateInventoryRequest createInventoryRequest) {
        // Service will throw ResourceNotFoundException, DuplicateResourceException, or InvalidOperationException
        Inventory inventoryToCreate = convertToEntity(createInventoryRequest);
        Inventory savedInventory = inventoryService.createInventory(inventoryToCreate);
        return new ResponseEntity<>(convertToDto(savedInventory), HttpStatus.CREATED);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<InventoryDTO> updateInventory(@PathVariable Long productId, @Valid @RequestBody UpdateInventoryRequest updateInventoryRequest) {
        // Service will throw ResourceNotFoundException
        Inventory existingInventory = new Inventory(); // Dummy entity
        Inventory updatedInventoryEntity = convertToEntity(updateInventoryRequest, existingInventory);
        Inventory savedInventory = inventoryService.updateInventory(productId, updatedInventoryEntity);
        return new ResponseEntity<>(convertToDto(savedInventory), HttpStatus.OK);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryDTO>> getLowStockProducts() {
        List<Inventory> lowStockProducts = inventoryService.getLowStockProducts();
        List<InventoryDTO> lowStockDTOs = lowStockProducts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(lowStockDTOs, HttpStatus.OK);
    }

    @DeleteMapping("/{id}") // Endpoint to delete inventory by its own inventoryId
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        // Service will throw ResourceNotFoundException
        inventoryService.deleteInventory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}