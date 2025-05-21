package com.example.inventory.entity;

import jakarta.persistence.*;
import lombok.Data; // Ensure Lombok is imported

@Entity
@Table(name = "products")
@Data // This provides getters and setters for all fields
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(name = "description")
    private String description;

    // This is the correct way to map ManyToOne relationships.
    // The foreign key column 'category_id' in 'products' table refers to 'categoryId' in 'categories' table.
    @ManyToOne(fetch = FetchType.EAGER) // EAGER fetch is fine for now, consider LAZY for performance in large apps.
    @JoinColumn(name = "category_id", referencedColumnName = "categoryId")
    private Category category; // This field holds the associated Category object.

    // Removed the direct 'private Long categoryId;' field.
    // Spring/Hibernate will infer it from the 'category' object.

    // This is the correct way to map ManyToOne relationships.
    // The foreign key column 'supplier_id' in 'products' table refers to 'supplierId' in 'suppliers' table.
    @ManyToOne(fetch = FetchType.EAGER) // EAGER fetch is fine for now.
    @JoinColumn(name = "supplier_id", referencedColumnName = "supplierId")
    private Supplier supplier; // This field holds the associated Supplier object.

    // Removed the direct 'private Long supplierId;' field.
    // Spring/Hibernate will infer it from the 'supplier' object.

    @Column(name = "purchase_price", nullable = false)
    private Double purchasePrice;

    @Column(name = "selling_price", nullable = false)
    private Double sellingPrice;
}