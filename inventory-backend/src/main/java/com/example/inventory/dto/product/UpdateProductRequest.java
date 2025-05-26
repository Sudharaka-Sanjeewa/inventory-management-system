package com.example.inventory.dto.product;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProductRequest {
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String name;

    @Size(max = 50, message = "SKU cannot exceed 50 characters")
    private String sku;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private Long categoryId;

    private Long supplierId;

    @PositiveOrZero(message = "Purchase price must be zero or positive")
    private Double purchasePrice;

    @PositiveOrZero(message = "Sale price must be zero or positive")
    private Double sellingPrice;
}