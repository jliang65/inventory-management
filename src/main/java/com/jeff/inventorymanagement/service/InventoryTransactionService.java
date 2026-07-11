package com.jeff.inventorymanagement.service;

import com.jeff.inventorymanagement.dto.InventoryTransactionResponse;
import com.jeff.inventorymanagement.dto.StockInOutRequest;
import com.jeff.inventorymanagement.entity.Inventory;
import com.jeff.inventorymanagement.entity.InventoryTransaction;
import com.jeff.inventorymanagement.entity.Location;
import com.jeff.inventorymanagement.entity.Product;
import com.jeff.inventorymanagement.entity.TransactionType;
import com.jeff.inventorymanagement.repository.InventoryRepository;
import com.jeff.inventorymanagement.repository.InventoryTransactionRepository;
import com.jeff.inventorymanagement.repository.LocationRepository;
import com.jeff.inventorymanagement.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class InventoryTransactionService {
    private final InventoryService inventoryService;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;

    public InventoryTransactionService(InventoryService inventoryService,
                                         InventoryRepository inventoryRepository,
                                         InventoryTransactionRepository inventoryTransactionRepository,
                                         ProductRepository productRepository,
                                         LocationRepository locationRepository) {
        this.inventoryService = inventoryService;
        this.inventoryRepository = inventoryRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.productRepository = productRepository;
        this.locationRepository = locationRepository;
    }

    public List<InventoryTransactionResponse> findFilteredAsResponse(
            Long productId, Long locationId, String type) {
        TransactionType transactionType = parseTransactionType(type);

        List<InventoryTransaction> results;
        if (productId != null && locationId != null) {
            results = inventoryTransactionRepository.findByProductIdAndLocationIdOrderByCreatedAtDesc(
                productId, locationId);
        } else if (productId != null) {
            results = inventoryTransactionRepository.findByProductIdOrderByCreatedAtDesc(productId);
        } else if (locationId != null) {
            results = inventoryTransactionRepository.findByLocationIdOrderByCreatedAtDesc(locationId);
        } else if (transactionType != null) {
            results = inventoryTransactionRepository.findByTransactionTypeOrderByCreatedAtDesc(transactionType);
        } else {
            results = inventoryTransactionRepository.findAllByOrderByCreatedAtDesc();
        }

        if (transactionType != null && (productId != null || locationId != null)) {
            results = results.stream()
                .filter(t -> t.getTransactionType() == transactionType)
                .toList();
        }

        return results.stream()
            .map(this::toResponse)
            .toList();
    }

    private TransactionType parseTransactionType(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        try {
            return TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid transaction type: " + type);
        }
    }

    public InventoryTransaction findById(Long id) {
        return inventoryTransactionRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory transaction not found"));
    }

    public InventoryTransactionResponse findByIdAsResponse(Long id) {
        return toResponse(findById(id));
    }

    public InventoryTransactionResponse toResponse(InventoryTransaction transaction) {
        InventoryTransactionResponse response = new InventoryTransactionResponse();
        response.setId(transaction.getId());
        response.setProductId(transaction.getProduct().getId());
        response.setProductName(transaction.getProduct().getName());
        response.setLocationId(transaction.getLocation().getId());
        response.setLocationName(transaction.getLocation().getName());
        response.setTransactionType(transaction.getTransactionType());
        response.setQuantityChange(transaction.getQuantity());
        response.setPreviousQuantity(transaction.getPreviousQuantity());
        response.setNewQuantity(transaction.getNewQuantity());
        response.setRelatedTransactionId(
            transaction.getRelatedTransaction() != null ? transaction.getRelatedTransaction().getId() : null);
        response.setReason(transaction.getReason());
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }

    @Transactional
    public InventoryTransactionResponse stockIn(StockInOutRequest request) {
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"));

        Location location = locationRepository.findById(request.getLocationId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Location not found"));

        Inventory inventory = inventoryService.findByProductIdAndLocationId(
                request.getProductId(), request.getLocationId())
            .orElseGet(() -> {
                Inventory newInventory = new Inventory();
                newInventory.setProduct(product);
                newInventory.setLocation(location);
                return inventoryRepository.save(newInventory);
            });

        int previousQuantity = inventory.getQuantity();
        int newQuantity = previousQuantity + request.getQuantity();
        inventory.setQuantity(newQuantity);

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(product);
        transaction.setLocation(location);
        transaction.setTransactionType(TransactionType.STOCK_IN);
        transaction.setQuantity(request.getQuantity());
        transaction.setPreviousQuantity(previousQuantity);
        transaction.setNewQuantity(newQuantity);
        transaction.setReason(request.getReason());

        inventoryRepository.save(inventory);
        InventoryTransaction record = inventoryTransactionRepository.save(transaction);

        return toResponse(record);
    }

    @Transactional
    public InventoryTransactionResponse stockOut(StockInOutRequest request) {
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"));

        Location location = locationRepository.findById(request.getLocationId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Location not found"));

        Inventory inventory = inventoryService.findByProductIdAndLocationId(
                request.getProductId(), request.getLocationId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "No inventory found for this product and location"));

        int previousQuantity = inventory.getQuantity();
        if (previousQuantity < request.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock");
        }

        int newQuantity = previousQuantity - request.getQuantity();
        inventory.setQuantity(newQuantity);

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(product);
        transaction.setLocation(location);
        transaction.setTransactionType(TransactionType.STOCK_OUT);
        transaction.setQuantity(request.getQuantity());
        transaction.setPreviousQuantity(previousQuantity);
        transaction.setNewQuantity(newQuantity);
        transaction.setReason(request.getReason());

        inventoryRepository.save(inventory);
        InventoryTransaction record = inventoryTransactionRepository.save(transaction);

        return toResponse(record);
    }
}
