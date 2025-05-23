package com.example.inventory.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateProductRequest {
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String name;

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU cannot exceed 50 characters")
    private String sku;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotNull(message = "Purchase price is required")
    @PositiveOrZero(message = "Purchase price must be zero or positive")
    private Double purchasePrice;

    @NotNull(message = "Sale price is required")
    @PositiveOrZero(message = "Sale price must be zero or positive")
    private Double salePrice;
}