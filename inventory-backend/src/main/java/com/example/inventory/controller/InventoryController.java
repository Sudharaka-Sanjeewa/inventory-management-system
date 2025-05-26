package com.example.inventory.controller;

import com.example.inventory.dto.inventory.CreateInventoryRequest;
import com.example.inventory.dto.inventory.InventoryDTO;
import com.example.inventory.dto.inventory.UpdateInventoryRequest;
import com.example.inventory.dto.product.ProductDTO; // For nested ProductDTO
import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.Product; // Import Product entity
import com.example.inventory.service.InventoryService;
import com.example.inventory.service.ProductService; // To fetch Product entity
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
    private ProductService productService; // Inject ProductService to fetch Product by ID

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
        // For simplicity, we are not deeply nesting CategoryDTO/SupplierDTO here.
        // If needed, you would also convert product.getCategory() to CategoryDTO etc.
        // For now, only basic product info is included in nested ProductDTO.
        return dto;
    }

    // Helper method to convert Inventory entity to InventoryDTO
    private InventoryDTO convertToDto(Inventory inventory) {
        InventoryDTO dto = new InventoryDTO();
        dto.setInventoryId(inventory.getInventoryId());
        dto.setProduct(convertProductToDto(inventory.getProduct())); // Convert nested Product
        dto.setQuantityInStock(inventory.getQuantityInStock());
        dto.setLowStockThreshold(inventory.getLowStockThreshold());
        dto.setCreatedAt(inventory.getCreatedAt());
        dto.setLastUpdated(inventory.getLastUpdated());
        return dto;
    }

    // Helper method to convert CreateInventoryRequest to Inventory entity for service layer
    private Inventory convertToEntity(CreateInventoryRequest dto) {
        Inventory inventory = new Inventory();
        // For creation, we only set the product ID on a new Product object.
        // The service layer is responsible for fetching the full Product entity.
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
        return inventoryService.getInventoryByProductId(productId)
                .map(this::convertToDto) // Convert entity to DTO if found
                .map(inventoryDTO -> new ResponseEntity<>(inventoryDTO, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<InventoryDTO> createInventory(@Valid @RequestBody CreateInventoryRequest createInventoryRequest) {
        try {
            // Convert DTO to entity. The product ID is embedded in the DTO.
            Inventory inventoryToCreate = convertToEntity(createInventoryRequest);
            Inventory savedInventory = inventoryService.createInventory(inventoryToCreate);
            // Convert saved entity back to DTO for response
            return new ResponseEntity<>(convertToDto(savedInventory), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Product ID missing, not found, or inventory exists for product
        }
    }

    @PutMapping("/{productId}")
    public ResponseEntity<InventoryDTO> updateInventory(@PathVariable Long productId, @Valid @RequestBody UpdateInventoryRequest updateInventoryRequest) {
        try {
            // Fetch existing inventory by product ID
            Inventory existingInventory = inventoryService.getInventoryByProductId(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Inventory not found for product ID: " + productId));

            // Update existing inventory fields from DTO
            Inventory updatedInventoryEntity = convertToEntity(updateInventoryRequest, existingInventory);
            Inventory savedInventory = inventoryService.updateInventory(productId, updatedInventoryEntity);

            // Convert saved entity back to DTO for response
            return new ResponseEntity<>(convertToDto(savedInventory), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Inventory not found for product ID
        }
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
        try {
            inventoryService.deleteInventory(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Inventory not found
        }
    }
}