package com.jeff.inventorymanagement.service;

import com.jeff.inventorymanagement.dto.PurchaseOrderItemRequest;
import com.jeff.inventorymanagement.dto.PurchaseOrderItemResponse;
import com.jeff.inventorymanagement.dto.PurchaseOrderItemUpdateRequest;
import com.jeff.inventorymanagement.dto.PurchaseOrderRequest;
import com.jeff.inventorymanagement.dto.PurchaseOrderResponse;
import com.jeff.inventorymanagement.dto.PurchaseOrderUpdateRequest;
import com.jeff.inventorymanagement.dto.StockInOutRequest;
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
    private final InventoryTransactionService inventoryTransactionService;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                PurchaseOrderItemRepository purchaseOrderItemRepository,
                                SupplierService supplierService,
                                LocationService locationService,
                                ProductService productService,
                                InventoryTransactionService inventoryTransactionService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.supplierService = supplierService;
        this.locationService = locationService;
        this.productService = productService;
        this.inventoryTransactionService = inventoryTransactionService;
    }

    public PurchaseOrder findById(Long id) {
        return purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found"));
    }

    public boolean isDraft(Long id) {
        return findById(id).getStatus() == PurchaseOrderStatus.DRAFT;
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
            .map(purchaseOrder -> {
                PurchaseOrderResponse response = toResponse(purchaseOrder);
                List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(purchaseOrder.getId());
                BigDecimal totalCost = items.stream()
                    .map(item -> item.getUnitCost().multiply(BigDecimal.valueOf(item.getOrderedQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                response.setTotalCost(totalCost);
                return response;
            });
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
        response.setLineTotal(item.getUnitCost().multiply(BigDecimal.valueOf(item.getOrderedQuantity())));
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

    @Transactional
    public PurchaseOrderResponse submit(Long id) {
        PurchaseOrder purchaseOrder = findById(id);
        
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only draft orders can be submitted");
        }

        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(id);
        if (items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot submit an order with no items");
        }
        
        purchaseOrder.setStatus(PurchaseOrderStatus.SUBMITTED);
        purchaseOrder.setOrderDate(LocalDateTime.now());
        purchaseOrderRepository.save(purchaseOrder);
        
        return findByIdAsResponse(id);
    }

    @Transactional
    public PurchaseOrderResponse receive(Long id) {
        PurchaseOrder purchaseOrder = findById(id);

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.SUBMITTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only submitted orders can be received");
        }

        //Inventory getting updated here
        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(id);
        Long destinationLocationId = purchaseOrder.getDestinationLocation().getId();

        for (PurchaseOrderItem item : items) {
            StockInOutRequest stockInRequest = new StockInOutRequest();
            stockInRequest.setProductId(item.getProduct().getId());
            stockInRequest.setLocationId(destinationLocationId);
            stockInRequest.setQuantity(item.getOrderedQuantity());
            stockInRequest.setReason("Received from PO #" + id);
            inventoryTransactionService.stockIn(stockInRequest);
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.RECEIVED);
        purchaseOrder.setReceivedAt(LocalDateTime.now());
        purchaseOrderRepository.save(purchaseOrder);

        return findByIdAsResponse(id);
    }

    @Transactional
    public PurchaseOrderResponse cancel(Long id) {
        PurchaseOrder purchaseOrder = findById(id);

        if (purchaseOrder.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Received orders cannot be cancelled");
        }

        if (purchaseOrder.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Order is already cancelled");
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.CANCELLED);
        purchaseOrderRepository.save(purchaseOrder);

        return findByIdAsResponse(id);
    }

    @Transactional
    public PurchaseOrderResponse update(Long id, PurchaseOrderUpdateRequest request) {
        PurchaseOrder purchaseOrder = findById(id);

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only draft orders can be updated");
        }

        if (!request.getSupplierId().equals(purchaseOrder.getSupplier().getId())) {
            List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(id);
            if (!items.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cannot change supplier on an order with items");
            }
            Supplier supplier = supplierService.findById(request.getSupplierId());
            purchaseOrder.setSupplier(supplier);
        }

        Location destinationLocation = locationService.findById(request.getDestinationLocationId());

        purchaseOrder.setDestinationLocation(destinationLocation);
        purchaseOrder.setNotes(request.getNotes());
        purchaseOrderRepository.save(purchaseOrder);

        return findByIdAsResponse(id);
    }

    @Transactional
    public PurchaseOrderItemResponse addItem(Long orderId, PurchaseOrderItemRequest request) {
        PurchaseOrder purchaseOrder = findById(orderId);

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Can only add items to draft orders");
        }

        Product product = productService.findById(request.getProductId());

        // Check if product already exists in this order
        boolean productExists = purchaseOrderItemRepository.findByPurchaseOrderId(orderId)
            .stream()
            .anyMatch(item -> item.getProduct().getId().equals(request.getProductId()));

        if (productExists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Product " + request.getProductId() + " is already in this order");
        }

        // Make sure the supplier actually sells that product
        if (product.getSupplier() == null ||
                !product.getSupplier().getId().equals(purchaseOrder.getSupplier().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Product " + product.getId() + " does not belong to supplier " + purchaseOrder.getSupplier().getId());
        }

        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setPurchaseOrder(purchaseOrder);
        item.setProduct(product);
        item.setOrderedQuantity(request.getOrderedQuantity());
        item.setUnitCost(request.getUnitCost());

        PurchaseOrderItem savedItem = purchaseOrderItemRepository.save(item);
        return toItemResponse(savedItem);
    }

    @Transactional
    public PurchaseOrderItemResponse updateItem(Long orderId, Long itemId, PurchaseOrderItemUpdateRequest request) {
        PurchaseOrder purchaseOrder = findById(orderId);

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Can only update items on draft orders");
        }

        PurchaseOrderItem item = purchaseOrderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order item not found"));

        if (!item.getPurchaseOrder().getId().equals(orderId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Item does not belong to the specified purchase order");
        }

        item.setOrderedQuantity(request.getOrderedQuantity());
        item.setUnitCost(request.getUnitCost());

        PurchaseOrderItem savedItem = purchaseOrderItemRepository.save(item);
        return toItemResponse(savedItem);
    }

    @Transactional
    public void deleteItem(Long orderId, Long itemId) {
        PurchaseOrder purchaseOrder = findById(orderId);

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Can only delete items from draft orders");
        }

        PurchaseOrderItem item = purchaseOrderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order item not found"));

        if (!item.getPurchaseOrder().getId().equals(orderId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Item does not belong to the specified purchase order");
        }

        purchaseOrderItemRepository.delete(item);
    }
}
