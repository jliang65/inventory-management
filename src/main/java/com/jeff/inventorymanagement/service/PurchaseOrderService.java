package com.jeff.inventorymanagement.service;

import com.jeff.inventorymanagement.dto.PurchaseOrderItemResponse;
import com.jeff.inventorymanagement.dto.PurchaseOrderResponse;
import com.jeff.inventorymanagement.entity.PurchaseOrder;
import com.jeff.inventorymanagement.entity.PurchaseOrderItem;
import com.jeff.inventorymanagement.entity.PurchaseOrderStatus;
import com.jeff.inventorymanagement.repository.PurchaseOrderItemRepository;
import com.jeff.inventorymanagement.repository.PurchaseOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                PurchaseOrderItemRepository purchaseOrderItemRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
    }

    public PurchaseOrder findById(Long id) {
        return purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found"));
    }

    public PurchaseOrderResponse findByIdAsResponse(Long id) {
        PurchaseOrder purchaseOrder = findById(id);
        PurchaseOrderResponse response = toResponse(purchaseOrder);
        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(id);
        response.setItems(items.stream().map(this::toItemResponse).toList());
        return response;
    }

    public Page<PurchaseOrderResponse> findFilteredAsResponse(
            Long supplierId, Long destinationLocationId, PurchaseOrderStatus status, Pageable pageable) {
        return purchaseOrderRepository.findFiltered(supplierId, destinationLocationId, status, pageable)
            .map(this::toResponse);
    }

    public PurchaseOrderResponse toResponse(PurchaseOrder purchaseOrder) {
        PurchaseOrderResponse response = new PurchaseOrderResponse();
        response.setId(purchaseOrder.getId());
        response.setSupplierId(purchaseOrder.getSupplier().getId());
        response.setSupplierName(purchaseOrder.getSupplier().getName());
        response.setDestinationLocationId(purchaseOrder.getDestinationLocation().getId());
        response.setDestinationLocationName(purchaseOrder.getDestinationLocation().getName());
        response.setStatus(purchaseOrder.getStatus());
        response.setOrderDate(purchaseOrder.getOrderDate());
        response.setExpectedDeliveryDate(purchaseOrder.getExpectedDeliveryDate());
        response.setReceivedAt(purchaseOrder.getReceivedAt());
        response.setNotes(purchaseOrder.getNotes());
        return response;
    }

    private PurchaseOrderItemResponse toItemResponse(PurchaseOrderItem item) {
        PurchaseOrderItemResponse response = new PurchaseOrderItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setProductSku(item.getProduct().getSku());
        response.setOrderedQuantity(item.getOrderedQuantity());
        response.setUnitCost(item.getUnitCost());
        return response;
    }
}
