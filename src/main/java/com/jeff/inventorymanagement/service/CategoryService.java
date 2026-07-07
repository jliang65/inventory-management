package com.jeff.inventorymanagement.service;

import com.jeff.inventorymanagement.entity.Category;
import com.jeff.inventorymanagement.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.jeff.inventorymanagement.dto.CategoryDto;


@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    // Add methods like findAll(), findById(), save(), deleteById()

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    public Category save(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already exists");
        }
        return categoryRepository.save(category);
    }

    public Category update(Long id, Category updatedCategory) {
        Category target = findById(id);
        
        // Check if name is not already taken
        if (!target.getName().equals(updatedCategory.getName()) 
                && categoryRepository.existsByName(updatedCategory.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already exists");
        }
        
        target.setName(updatedCategory.getName());
        return categoryRepository.save(target);
    }

    public void delete(Long id) {
        Category category = findById(id);
        categoryRepository.delete(category);
    }

    public CategoryDto toDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }

    public Category toEntity(CategoryDto dto) {
        Category category = new Category();
        category.setName(dto.getName());
        return category;
    }

    public CategoryDto findByIdAsDto(Long id) {
        return toDto(findById(id));
    }

    public CategoryDto saveFromDto(CategoryDto dto) {
        Category category = toEntity(dto);
        return toDto(save(category));
    }

    public List<CategoryDto> findAllAsDto() {
        return findAll().stream()
            .map(this::toDto)
            .toList();
    }

    public CategoryDto updateFromDto(Long id, CategoryDto dto) {
        Category target = findById(id);
        
        if (!target.getName().equals(dto.getName()) 
                && categoryRepository.existsByName(dto.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already exists");
        }
        
        target.setName(dto.getName());
        return toDto(categoryRepository.save(target));
    }
}
