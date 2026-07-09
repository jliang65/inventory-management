package com.jeff.inventorymanagement.controller;

import com.jeff.inventorymanagement.dto.InventoryResponse;
import com.jeff.inventorymanagement.service.InventoryService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<InventoryResponse> getAllInventory() {
        return inventoryService.findAllAsResponse();
    }

    @GetMapping("/{id}")
    public InventoryResponse getInventoryById(@PathVariable Long id) {
        return inventoryService.findByIdAsResponse(id);
    }

    @GetMapping("/products/{productId}")
    public List<InventoryResponse> getInventoryByProductId(@PathVariable Long productId) {
        return inventoryService.findByProductIdAsResponse(productId);
    }

    @GetMapping("/locations/{locationId}")
    public List<InventoryResponse> getInventoryByLocationId(@PathVariable Long locationId) {
        return inventoryService.findByLocationIdAsResponse(locationId);
    }
    
    @GetMapping("/low-stock")
    public List<InventoryResponse> getLowStockInventory() {
        return inventoryService.findLowStockAsResponse();
    }
}