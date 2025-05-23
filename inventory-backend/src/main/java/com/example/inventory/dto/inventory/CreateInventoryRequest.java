package com.example.inventory.dto.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CreateInventoryRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity in stock is required")
    @Min(value = 0, message = "Quantity in stock cannot be negative")
    private Integer quantityInStock;

    @NotNull(message = "Low stock threshold is required")
    @Min(value = 0, message = "Low stock threshold cannot be negative")
    private Integer lowStockThreshold;
}