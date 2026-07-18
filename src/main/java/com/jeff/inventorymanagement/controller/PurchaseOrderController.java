package com.jeff.inventorymanagement.controller;

import com.jeff.inventorymanagement.dto.PurchaseOrderItemRequest;
import com.jeff.inventorymanagement.dto.PurchaseOrderItemResponse;
import com.jeff.inventorymanagement.dto.PurchaseOrderRequest;
import com.jeff.inventorymanagement.dto.PurchaseOrderResponse;
import com.jeff.inventorymanagement.dto.PurchaseOrderUpdateRequest;
import com.jeff.inventorymanagement.entity.PurchaseOrderStatus;
import com.jeff.inventorymanagement.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @GetMapping
    public Page<PurchaseOrderResponse> getPurchaseOrders(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long destinationLocationId,
            @RequestParam(required = false) PurchaseOrderStatus status,
            Pageable pageable) {
        return purchaseOrderService.findFilteredAsResponse(supplierId, destinationLocationId, status, pageable);
    }

    @GetMapping("/{id}")
    public PurchaseOrderResponse getPurchaseOrderById(@PathVariable Long id) {
        return purchaseOrderService.findByIdAsResponse(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseOrderResponse createPurchaseOrder(@Valid @RequestBody PurchaseOrderRequest request) {
        return purchaseOrderService.saveFromRequest(request);
    }

    @PostMapping("/{id}/submit")
    public PurchaseOrderResponse submitPurchaseOrder(@PathVariable Long id) {
        return purchaseOrderService.submit(id);
    }

    @PostMapping("/{id}/receive")
    public PurchaseOrderResponse receivePurchaseOrder(@PathVariable Long id) {
        return purchaseOrderService.receive(id);
    }

    @PostMapping("/{id}/cancel")
    public PurchaseOrderResponse cancelPurchaseOrder(@PathVariable Long id) {
        return purchaseOrderService.cancel(id);
    }

    @PostMapping("/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseOrderItemResponse addItemToPurchaseOrder(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderItemRequest request) {
        return purchaseOrderService.addItem(id, request);
    }

    @PutMapping("/{id}")
    public PurchaseOrderResponse updatePurchaseOrder(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderUpdateRequest request) {
        return purchaseOrderService.update(id, request);
    }
}
