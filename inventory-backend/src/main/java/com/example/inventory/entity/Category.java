package com.example.inventory.entity;

import jakarta.persistence.*;
import lombok.Data; // Ensure Lombok is imported

@Entity
@Table(name = "categories")
@Data // This provides getters and setters (e.g., getCategoryId(), getName())
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "categoryId") // Matches the referencedColumnName in Product.java
    private Long categoryId;

    @Column(name = "name", nullable = false, unique = true)
    private String name;
}