package com.jeff.inventorymanagement.dto;

import jakarta.validation.constraints.NotBlank;

public class CategoryDto {
    private Long id;
    
    @NotBlank(message = "Category name is required")
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}