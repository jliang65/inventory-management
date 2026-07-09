package com.jeff.inventorymanagement.service;

import com.jeff.inventorymanagement.dto.InventoryResponse;
import com.jeff.inventorymanagement.entity.Inventory;
import com.jeff.inventorymanagement.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<Inventory> findAll() {
        return inventoryRepository.findAll();
    }

    public Inventory findById(Long id) {
        return inventoryRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found"));
    }

    public InventoryResponse findByIdAsResponse(Long id) {
        return toResponse(findById(id));
    }

    public List<InventoryResponse> findAllAsResponse() {
        return findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    public InventoryResponse toResponse(Inventory inventory) {
        InventoryResponse response = new InventoryResponse();
        response.setId(inventory.getId());
        response.setProductId(inventory.getProduct().getId());
        response.setProductName(inventory.getProduct().getName());
        response.setLocationId(inventory.getLocation().getId());
        response.setLocationName(inventory.getLocation().getName());
        response.setQuantity(inventory.getQuantity());
        return response;
    }

    public List<Inventory> findByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }

    public List<InventoryResponse> findByProductIdAsResponse(Long productId) {
        return findByProductId(productId).stream()
            .map(this::toResponse)
            .toList();
    }

    public List<Inventory> findByLocationId(Long locationId){
        return inventoryRepository.findByLocationId(locationId);
    }

    public List<InventoryResponse> findByLocationIdAsResponse(Long locationId) {
        return findByLocationId(locationId).stream()
            .map(this::toResponse)
            .toList();
    }
}