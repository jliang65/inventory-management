package com.jeff.inventorymanagement.controller;

import com.jeff.inventorymanagement.dto.InventoryRequest;
import com.jeff.inventorymanagement.dto.InventoryResponse;
import com.jeff.inventorymanagement.dto.InventoryTransactionResponse;
import com.jeff.inventorymanagement.dto.StockAdjustRequest;
import com.jeff.inventorymanagement.dto.StockInOutRequest;
import com.jeff.inventorymanagement.dto.StockTransferRequest;
import com.jeff.inventorymanagement.service.InventoryService;
import com.jeff.inventorymanagement.service.InventoryTransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryTransactionService inventoryTransactionService;

    public InventoryController(InventoryService inventoryService,
                               InventoryTransactionService inventoryTransactionService) {
        this.inventoryService = inventoryService;
        this.inventoryTransactionService = inventoryTransactionService;
    }

    @PostMapping("/stock-in")
    public InventoryTransactionResponse stockIn(@Valid @RequestBody StockInOutRequest request) {
        return inventoryTransactionService.stockIn(request);
    }

    @PostMapping("/stock-out")
    public InventoryTransactionResponse stockOut(@Valid @RequestBody StockInOutRequest request) {
        return inventoryTransactionService.stockOut(request);
    }

    @PostMapping("/adjust")
    public InventoryTransactionResponse adjust(@Valid @RequestBody StockAdjustRequest request) {
        return inventoryTransactionService.adjust(request);
    }

    @PostMapping("/transfer")
    public List<InventoryTransactionResponse> transfer(@Valid @RequestBody StockTransferRequest request) {
        return inventoryTransactionService.transfer(request);
    }

    @GetMapping("/transactions")
    public List<InventoryTransactionResponse> getTransactions(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String type) {
        return inventoryTransactionService.findFilteredAsResponse(productId, locationId, type);
    }

    @GetMapping("/transactions/{id}")
    public InventoryTransactionResponse getTransactionById(@PathVariable Long id) {
        return inventoryTransactionService.findByIdAsResponse(id);
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
