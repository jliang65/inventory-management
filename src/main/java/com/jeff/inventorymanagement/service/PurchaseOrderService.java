package com.jeff.inventorymanagement.service;

import com.jeff.inventorymanagement.dto.PurchaseOrderItemRequest;
import com.jeff.inventorymanagement.dto.PurchaseOrderItemResponse;
import com.jeff.inventorymanagement.dto.PurchaseOrderRequest;
import com.jeff.inventorymanagement.dto.PurchaseOrderResponse;
import com.jeff.inventorymanagement.entity.Location;
import com.jeff.inventorymanagement.entity.Product;
import com.jeff.inventorymanagement.entity.PurchaseOrder;
import com.jeff.inventorymanagement.entity.PurchaseOrderItem;
import com.jeff.inventorymanagement.entity.PurchaseOrderStatus;
import com.jeff.inventorymanagement.entity.Supplier;
import com.jeff.inventorymanagement.repository.PurchaseOrderItemRepository;
import com.jeff.inventorymanagement.repository.PurchaseOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final SupplierService supplierService;
    private final LocationService locationService;
    private final ProductService productService;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                PurchaseOrderItemRepository purchaseOrderItemRepository,
                                SupplierService supplierService,
                                LocationService locationService,
                                ProductService productService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.supplierService = supplierService;
        this.locationService = locationService;
        this.productService = productService;
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

        BigDecimal totalCost = items.stream()
            .map(item -> item.getUnitCost().multiply(BigDecimal.valueOf(item.getOrderedQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalCost(totalCost);

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

    @Transactional
    public PurchaseOrderResponse saveFromRequest(PurchaseOrderRequest request) {
        Supplier supplier = supplierService.findById(request.getSupplierId());
        Location destinationLocation = locationService.findById(request.getDestinationLocationId());

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setDestinationLocation(destinationLocation);
        purchaseOrder.setStatus(PurchaseOrderStatus.DRAFT);
        purchaseOrder.setOrderDate(LocalDateTime.now());
        purchaseOrder.setNotes(request.getNotes());

        PurchaseOrder savedOrder = purchaseOrderRepository.save(purchaseOrder);

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (PurchaseOrderItemRequest itemRequest : request.getItems()) {
                Product product = productService.findById(itemRequest.getProductId());

                if (product.getSupplier() == null ||
                        !product.getSupplier().getId().equals(supplier.getId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Product " + product.getId() + " does not belong to supplier " + supplier.getId());
                }

                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setPurchaseOrder(savedOrder);
                item.setProduct(product);
                item.setOrderedQuantity(itemRequest.getOrderedQuantity());
                item.setUnitCost(itemRequest.getUnitCost());

                purchaseOrderItemRepository.save(item);
            }
        }

        return findByIdAsResponse(savedOrder.getId());
    }
}
