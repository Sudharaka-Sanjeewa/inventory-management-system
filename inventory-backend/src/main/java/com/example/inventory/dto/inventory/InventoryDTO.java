package com.example.inventory.dto.inventory;

import com.example.inventory.dto.product.ProductDTO;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InventoryDTO {
    private Long inventoryId;
    private ProductDTO product; // Nested DTO
    private Integer quantityInStock;
    private Integer lowStockThreshold;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
}