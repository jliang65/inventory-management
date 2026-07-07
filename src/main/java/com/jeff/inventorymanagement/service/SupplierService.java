package com.jeff.inventorymanagement.service;

import com.jeff.inventorymanagement.dto.SupplierDto;
import com.jeff.inventorymanagement.entity.Supplier;
import com.jeff.inventorymanagement.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import java.util.List;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    public List<SupplierDto> findAllAsDto() {
        return findAll().stream()
            .map(this::toDto)
            .toList();
    }

    public Supplier findById(Long id) {
        return supplierRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));
    }
    
    public Supplier save(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    public Supplier update(Long id, Supplier updatedSupplier) {
        Supplier target = findById(id);
        target.setName(updatedSupplier.getName());
        target.setContactName(updatedSupplier.getContactName());
        target.setEmail(updatedSupplier.getEmail());
        target.setPhone(updatedSupplier.getPhone());
        target.setAddress(updatedSupplier.getAddress());
        target.setActive(updatedSupplier.getActive());
        return supplierRepository.save(target);
    }

    public void deleteById(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found");
        }
        supplierRepository.deleteById(id);
    }

    public SupplierDto toDto(Supplier supplier) {
        SupplierDto dto = new SupplierDto();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setContactName(supplier.getContactName());
        dto.setEmail(supplier.getEmail());
        dto.setPhone(supplier.getPhone());
        dto.setAddress(supplier.getAddress());
        dto.setActive(supplier.getActive());
        return dto;
    }

    public Supplier toEntity(SupplierDto dto) {
        Supplier supplier = new Supplier();
        supplier.setName(dto.getName());
        supplier.setContactName(dto.getContactName());
        supplier.setEmail(dto.getEmail());
        supplier.setPhone(dto.getPhone());
        supplier.setAddress(dto.getAddress());
        supplier.setActive(dto.getActive() != null ? dto.getActive() : true);
        return supplier;
    }

    public SupplierDto findByIdAsDto(Long id) {
        return toDto(findById(id));
    }

    public SupplierDto saveFromDto(SupplierDto dto) {
        Supplier supplier = toEntity(dto);
        return toDto(save(supplier));
    }

    public SupplierDto updateFromDto(Long id, SupplierDto dto) {
        Supplier target = findById(id);
        target.setName(dto.getName());
        target.setContactName(dto.getContactName());
        target.setEmail(dto.getEmail());
        target.setPhone(dto.getPhone());
        target.setAddress(dto.getAddress());
        target.setActive(dto.getActive());
        return toDto(supplierRepository.save(target));
    }
}