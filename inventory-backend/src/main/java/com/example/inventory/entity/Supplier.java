package com.example.inventory.entity;

import jakarta.persistence.*;
import lombok.Data; // Ensure Lombok is imported

@Entity
@Table(name = "suppliers")
@Data // This provides getters and setters (e.g., getSupplierId(), getName(), getContactInfo())
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplierId") // Matches the referencedColumnName in Product.java
    private Long supplierId;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "contact_info")
    private String contactInfo;
}