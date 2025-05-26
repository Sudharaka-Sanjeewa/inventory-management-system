package com.example.inventory.controller;

import com.example.inventory.dto.category.CategoryDTO;
import com.example.inventory.dto.category.CreateCategoryRequest;
import com.example.inventory.dto.category.UpdateCategoryRequest;
import com.example.inventory.entity.Category;
import com.example.inventory.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
        if (dto.getName() != null) {
            existingCategory.setName(dto.getName());
        }
        return existingCategory;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(categoryDTOs, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CreateCategoryRequest createCategoryRequest) {
        // Service will throw DuplicateResourceException if name exists
        Category categoryToCreate = convertToEntity(createCategoryRequest);
        Category savedCategory = categoryService.createCategory(categoryToCreate);
        return new ResponseEntity<>(convertToDto(savedCategory), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        // Service will throw ResourceNotFoundException if not found
        Category category = categoryService.getCategoryById(id);
        return new ResponseEntity<>(convertToDto(category), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest updateCategoryRequest) {
        // Service will throw ResourceNotFoundException or DuplicateResourceException
        Category existingCategory = new Category(); // Create a dummy entity to pass to convertToEntity
        // The actual existing category is fetched by the service layer
        Category updatedCategoryEntity = convertToEntity(updateCategoryRequest, existingCategory);
        Category savedCategory = categoryService.updateCategory(id, updatedCategoryEntity);
        return new ResponseEntity<>(convertToDto(savedCategory), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        // Service will throw ResourceNotFoundException or InvalidOperationException
        categoryService.deleteCategory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}