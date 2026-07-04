package com.jeff.inventorymanagement.controller;

import com.jeff.inventorymanagement.entity.Category;
import com.jeff.inventorymanagement.service.CategoryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // Add endpoints: @GetMapping, @PostMapping, @PutMapping, @DeleteMapping
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.findAll();
    }
}