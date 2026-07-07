package com.jeff.inventorymanagement.controller;

import com.jeff.inventorymanagement.dto.SupplierDto;
import com.jeff.inventorymanagement.service.SupplierService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PutMapping;

import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public List<SupplierDto> getAllSuppliers() {
        return supplierService.findAllAsDto();
    }

    @GetMapping("/{id}")
    public SupplierDto getSupplierById(@PathVariable Long id) {
        return supplierService.findByIdAsDto(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierDto createSupplier(@Valid @RequestBody SupplierDto supplier) {
        return supplierService.saveFromDto(supplier);
    }

    @PutMapping("/{id}")
    public SupplierDto updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierDto supplier) {
        return supplierService.updateFromDto(id, supplier);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSupplier(@PathVariable Long id) {
        supplierService.deleteById(id);
    }
}