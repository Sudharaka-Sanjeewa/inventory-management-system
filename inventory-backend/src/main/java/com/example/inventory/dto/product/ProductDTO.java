package com.example.inventory.dto.product;

import com.example.inventory.dto.category.CategoryDTO;
import com.example.inventory.dto.supplier.SupplierDTO;
import lombok.Data;

@Data
public class ProductDTO {
    private Long productId;
    private String name;
    private String sku;
    private String description;
    private CategoryDTO category; // Nested DTO
    private SupplierDTO supplier; // Nested DTO
    private Double purchasePrice;
    private Double sellingPrice;
}