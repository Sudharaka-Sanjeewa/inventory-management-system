package com.example.inventory.controller;

import com.example.inventory.dto.product.CreateProductRequest;
import com.example.inventory.dto.product.ProductDTO;
import com.example.inventory.dto.product.UpdateProductRequest;
import com.example.inventory.dto.category.CategoryDTO;
import com.example.inventory.dto.supplier.SupplierDTO;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.Category;
import com.example.inventory.entity.Supplier;
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
        dto.setSellingPrice(product.getSellingPrice());
        dto.setCategory(convertCategoryToDto(product.getCategory()));
        dto.setSupplier(convertSupplierToDto(product.getSupplier()));
        return dto;
    }

    // Helper method to convert CreateProductRequest to Product entity
    private Product convertToEntity(CreateProductRequest dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setSku(dto.getSku());
        product.setDescription(dto.getDescription());
        product.setPurchasePrice(dto.getPurchasePrice());
        product.setSellingPrice(dto.getSellingPrice());

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
        if (dto.getSellingPrice() != null) existingProduct.setSellingPrice(dto.getSellingPrice());

        if (dto.getCategoryId() != null) {
            Category category = new Category();
            category.setCategoryId(dto.getCategoryId());
            existingProduct.setCategory(category);
        }
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
        // Service will throw ResourceNotFoundException if not found
        Product product = productService.getProductById(id);
        return new ResponseEntity<>(convertToDto(product), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductRequest createProductRequest) {
        // Service will throw ResourceNotFoundException, DuplicateResourceException, or InvalidOperationException
        Product productToCreate = convertToEntity(createProductRequest);
        Product savedProduct = productService.createProduct(productToCreate);
        return new ResponseEntity<>(convertToDto(savedProduct), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest updateProductRequest) {
        // Service will throw ResourceNotFoundException, DuplicateResourceException, or InvalidOperationException
        Product existingProduct = new Product(); // Dummy entity
        Product updatedProductEntity = convertToEntity(updateProductRequest, existingProduct);
        Product savedProduct = productService.updateProduct(id, updatedProductEntity);
        return new ResponseEntity<>(convertToDto(savedProduct), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        // Service will throw ResourceNotFoundException or InvalidOperationException
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}