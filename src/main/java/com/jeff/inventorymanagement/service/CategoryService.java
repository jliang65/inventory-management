package com.jeff.inventorymanagement.service;

import com.jeff.inventorymanagement.entity.Category;
import com.jeff.inventorymanagement.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;

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
}
