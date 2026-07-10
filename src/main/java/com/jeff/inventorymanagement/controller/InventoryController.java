package com.jeff.inventorymanagement.controller;

import com.jeff.inventorymanagement.dto.InventoryRequest;
import com.jeff.inventorymanagement.dto.InventoryResponse;
import com.jeff.inventorymanagement.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<InventoryResponse> getInventory(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long locationId) {
        return inventoryService.findFilteredAsResponse(productId, locationId);
    }

    @GetMapping("/{id}")
    public InventoryResponse getInventoryById(@PathVariable Long id) {
        return inventoryService.findByIdAsResponse(id);
    }

    @GetMapping("/low-stock")
    public List<InventoryResponse> getLowStockInventory() {
        return inventoryService.findLowStockAsResponse();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse createInventory(@Valid @RequestBody InventoryRequest request) {
        return inventoryService.saveFromRequest(request);
    }
}