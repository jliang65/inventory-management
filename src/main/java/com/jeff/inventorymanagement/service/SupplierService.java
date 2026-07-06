package com.jeff.inventorymanagement.service;

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
}