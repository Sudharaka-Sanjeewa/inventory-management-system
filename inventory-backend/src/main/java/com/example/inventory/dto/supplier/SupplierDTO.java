package com.example.inventory.dto.supplier;

import lombok.Data;

@Data
public class SupplierDTO {
    private Long supplierId;
    private String name;
    private String contactInfo;
}