package com.example.inventory.controller;

import com.example.inventory.dto.category.CategoryDTO;
import com.example.inventory.dto.category.CreateCategoryRequest;
import com.example.inventory.dto.category.UpdateCategoryRequest;
import com.example.inventory.entity.Category;
import com.example.inventory.service.CategoryService;
import jakarta.validation.Valid; // Import for @Valid annotation
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors; // For stream API to map entities to DTOs

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // Helper method to convert Category entity to CategoryDTO
    private CategoryDTO convertToDto(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setName(category.getName());
        return dto;
    }

    // Helper method to convert CreateCategoryRequest to Category entity
    private Category convertToEntity(CreateCategoryRequest dto) {
        Category category = new Category();
        category.setName(dto.getName());
        return category;
    }

    // Helper method to update Category entity from UpdateCategoryRequest
    private Category convertToEntity(UpdateCategoryRequest dto, Category existingCategory) {
        // Only update fields that are provided and valid
        if (dto.getName() != null) {
            existingCategory.setName(dto.getName());
        }
        return existingCategory;
    }


    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        // Convert list of entities to list of DTOs
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(categoryDTOs, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CreateCategoryRequest createCategoryRequest) {
        try {
            // Convert DTO to entity before passing to service
            Category categoryToCreate = convertToEntity(createCategoryRequest);
            Category savedCategory = categoryService.createCategory(categoryToCreate);
            // Convert saved entity back to DTO for response
            return new ResponseEntity<>(convertToDto(savedCategory), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // In a real production app, you'd use @ControllerAdvice for centralized error handling
            // For now, this approach demonstrates the DTO usage
            return new ResponseEntity<>(HttpStatus.CONFLICT); // 409 Conflict for duplicate name
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(this::convertToDto) // Convert entity to DTO if found
                .map(categoryDTO -> new ResponseEntity<>(categoryDTO, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest updateCategoryRequest) {
        try {
            // Fetch existing category, then update its fields from DTO
            // This is where DTO to entity conversion happens before passing to service
            Category existingCategory = categoryService.getCategoryById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));

            Category updatedCategoryEntity = convertToEntity(updateCategoryRequest, existingCategory);
            Category savedCategory = categoryService.updateCategory(id, updatedCategoryEntity);

            // Convert saved entity back to DTO for response
            return new ResponseEntity<>(convertToDto(savedCategory), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Category not found or validation error
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Category not found
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT); // For associated products/business logic conflict
        }
    }
}