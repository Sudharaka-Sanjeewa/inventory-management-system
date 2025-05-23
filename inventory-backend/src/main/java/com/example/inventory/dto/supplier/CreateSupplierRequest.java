package com.example.inventory.dto.supplier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSupplierRequest {
    @NotBlank(message = "Supplier name is required")
    @Size(max = 100, message = "Supplier name cannot exceed 100 characters")
    private String name;
    @Size(max = 255, message = "Contact info cannot exceed 255 characters")
    private String contactInfo;
}