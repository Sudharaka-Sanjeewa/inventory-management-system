package com.example.inventory.controller;

import com.example.inventory.dto.product.CreateProductRequest;
import com.example.inventory.dto.product.ProductDTO;
import com.example.inventory.dto.product.UpdateProductRequest;
import com.example.inventory.dto.category.CategoryDTO; // For nested DTO
import com.example.inventory.dto.supplier.SupplierDTO; // For nested DTO
import com.example.inventory.entity.Product;
import com.example.inventory.entity.Category; // Import Category entity
import com.example.inventory.entity.Supplier; // Import Supplier entity
import com.example.inventory.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // Helper method to convert Category entity to CategoryDTO
    private CategoryDTO convertCategoryToDto(Category category) {
        if (category == null) return null;
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setName(category.getName());
        return dto;
    }

    // Helper method to convert Supplier entity to SupplierDTO
    private SupplierDTO convertSupplierToDto(Supplier supplier) {
        if (supplier == null) return null;
        SupplierDTO dto = new SupplierDTO();
        dto.setSupplierId(supplier.getSupplierId());
        dto.setName(supplier.getName());
        dto.setContactInfo(supplier.getContactInfo());
        return dto;
    }

    // Helper method to convert Product entity to ProductDTO
    private ProductDTO convertToDto(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setSku(product.getSku());
        dto.setDescription(product.getDescription());
        dto.setPurchasePrice(product.getPurchasePrice());
        dto.setSellingPrice(product.getSellingPrice()); // Corrected to sellingPrice
        dto.setCategory(convertCategoryToDto(product.getCategory())); // Convert nested Category
        dto.setSupplier(convertSupplierToDto(product.getSupplier())); // Convert nested Supplier
        return dto;
    }

    // Helper method to convert CreateProductRequest to Product entity
    private Product convertToEntity(CreateProductRequest dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setSku(dto.getSku());
        product.setDescription(dto.getDescription());
        product.setPurchasePrice(dto.getPurchasePrice());
        product.setSellingPrice(dto.getSellingPrice()); // Corrected to sellingPrice

        // For category and supplier, create a reference entity with just the ID
        // The service layer will fetch the full entity from the database
        if (dto.getCategoryId() != null) {
            Category category = new Category();
            category.setCategoryId(dto.getCategoryId());
            product.setCategory(category);
        }
        if (dto.getSupplierId() != null) {
            Supplier supplier = new Supplier();
            supplier.setSupplierId(dto.getSupplierId());
            product.setSupplier(supplier);
        }
        return product;
    }

    // Helper method to update Product entity from UpdateProductRequest
    private Product convertToEntity(UpdateProductRequest dto, Product existingProduct) {
        if (dto.getName() != null) existingProduct.setName(dto.getName());
        if (dto.getSku() != null) existingProduct.setSku(dto.getSku());
        if (dto.getDescription() != null) existingProduct.setDescription(dto.getDescription());
        if (dto.getPurchasePrice() != null) existingProduct.setPurchasePrice(dto.getPurchasePrice());
        if (dto.getSellingPrice() != null) existingProduct.setSellingPrice(dto.getSellingPrice()); // Corrected to sellingPrice

        // Update category if ID is provided
        if (dto.getCategoryId() != null) {
            Category category = new Category();
            category.setCategoryId(dto.getCategoryId());
            existingProduct.setCategory(category);
        }
        // Update supplier if ID is provided
        if (dto.getSupplierId() != null) {
            Supplier supplier = new Supplier();
            supplier.setSupplierId(dto.getSupplierId());
            existingProduct.setSupplier(supplier);
        }
        return existingProduct;
    }


    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(productDTOs, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(this::convertToDto) // Convert entity to DTO if found
                .map(productDTO -> new ResponseEntity<>(productDTO, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductRequest createProductRequest) {
        try {
            // Convert DTO to entity before passing to service
            Product productToCreate = convertToEntity(createProductRequest);
            Product savedProduct = productService.createProduct(productToCreate);
            // Convert saved entity back to DTO for response
            return new ResponseEntity<>(convertToDto(savedProduct), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Invalid category/supplier ID or SKU conflict
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest updateProductRequest) {
        try {
            // Fetch existing product, then update its fields from DTO
            Product existingProduct = productService.getProductById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

            Product updatedProductEntity = convertToEntity(updateProductRequest, existingProduct);
            Product savedProduct = productService.updateProduct(id, updatedProductEntity);

            // Convert saved entity back to DTO for response
            return new ResponseEntity<>(convertToDto(savedProduct), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Product not found or invalid related ID
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Product not found
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT); // For existing inventory
        }
    }
}