package com.example.inventory.service;

import com.example.inventory.entity.Category;
import com.example.inventory.repository.CategoryRepository;
import com.example.inventory.repository.ProductRepository; // Import ProductRepository
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.exception.DuplicateResourceException;
import com.example.inventory.exception.InvalidOperationException; // For deletion check
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository; // Inject ProductRepository

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) { // Changed return type to Category and removed Optional
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
    }

    @Transactional
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new DuplicateResourceException("Category with name '" + category.getName() + "' already exists.");
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, Category updatedCategory) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        // Check for duplicate name during update, but allow if it's the existing category's name
        if (categoryRepository.existsByName(updatedCategory.getName()) &&
                !existingCategory.getName().equals(updatedCategory.getName())) {
            throw new DuplicateResourceException("Category with name '" + updatedCategory.getName() + "' already exists.");
        }

        existingCategory.setName(updatedCategory.getName());
        return categoryRepository.save(existingCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        // First, check if the category exists
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with ID: " + id);
        }

        // Check for associated Products before deletion
        if (!productRepository.findByCategory_CategoryId(id).isEmpty()) {
            throw new InvalidOperationException("Cannot delete category with existing products. Delete associated products first.");
        }

        categoryRepository.deleteById(id);
    }
}