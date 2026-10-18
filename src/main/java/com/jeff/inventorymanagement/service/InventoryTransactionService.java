package com.jeff.inventorymanagement.service;

import com.jeff.inventorymanagement.dto.InventoryTransactionResponse;
import com.jeff.inventorymanagement.dto.StockAdjustRequest;
import com.jeff.inventorymanagement.dto.StockInOutRequest;
import com.jeff.inventorymanagement.dto.StockTransferRequest;
import com.jeff.inventorymanagement.entity.Inventory;
import com.jeff.inventorymanagement.entity.InventoryTransaction;
import com.jeff.inventorymanagement.entity.Location;
import com.jeff.inventorymanagement.entity.Product;
import com.jeff.inventorymanagement.entity.TransactionType;
import com.jeff.inventorymanagement.entity.User;
import com.jeff.inventorymanagement.repository.InventoryRepository;
import com.jeff.inventorymanagement.repository.InventoryTransactionRepository;
import com.jeff.inventorymanagement.repository.LocationRepository;
import com.jeff.inventorymanagement.repository.ProductRepository;
import com.jeff.inventorymanagement.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryTransactionService {
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    public InventoryTransactionService(InventoryRepository inventoryRepository,
                                         InventoryTransactionRepository inventoryTransactionRepository,
                                         ProductRepository productRepository,
                                         LocationRepository locationRepository,
                                         UserRepository userRepository) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.productRepository = productRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return userRepository.findByEmailIgnoreCase(auth.getName()).orElse(null);
    }

    public Page<InventoryTransactionResponse> findFilteredAsResponse(
            Long productId, Long locationId, String type,
            LocalDate startDate, LocalDate endDate, Pageable pageable) {
        TransactionType transactionType = parseTransactionType(type);

        LocalDateTime startDateTime =
            startDate == null ? null : startDate.atStartOfDay();
        LocalDateTime endDateTime =
            endDate == null ? null : endDate.plusDays(1).atStartOfDay();

        return inventoryTransactionRepository.findFiltered(
                productId, locationId, transactionType, startDateTime, endDateTime, pageable)
            .map(this::toResponse);
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
        response.setProductSku(transaction.getProduct().getSku());
        response.setLocationId(transaction.getLocation().getId());
        response.setLocationName(transaction.getLocation().getName());
        response.setTransactionType(transaction.getTransactionType());
        response.setQuantityChange(transaction.getQuantity());
        response.setPreviousQuantity(transaction.getPreviousQuantity());
        response.setNewQuantity(transaction.getNewQuantity());
        response.setRelatedTransactionId(
            transaction.getRelatedTransaction() != null ? transaction.getRelatedTransaction().getId() : null);
        response.setReason(transaction.getReason());
        if (transaction.getPerformedBy() != null) {
            response.setPerformedByUserId(transaction.getPerformedBy().getId());
            response.setPerformedByEmail(transaction.getPerformedBy().getEmail());
        }
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }

    @Transactional
    public InventoryTransactionResponse stockIn(StockInOutRequest request) {
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"));

        Location location = locationRepository.findById(request.getLocationId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Location not found"));

        // Lock the row, then read and update quantity. A missing row is created below.
        Inventory inventory = inventoryRepository.findByProductIdAndLocationIdForUpdate(
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
        transaction.setPerformedBy(getCurrentUser());

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

        // Lock the row, then read and update quantity.
        Inventory inventory = inventoryRepository.findByProductIdAndLocationIdForUpdate(
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
        transaction.setPerformedBy(getCurrentUser());

        inventoryRepository.save(inventory);
        InventoryTransaction record = inventoryTransactionRepository.save(transaction);

        return toResponse(record);
    }

    @Transactional
    public InventoryTransactionResponse adjust(StockAdjustRequest request) {
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"));

        Location location = locationRepository.findById(request.getLocationId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Location not found"));

        // Lock the row, then read and update quantity. A missing row is created below.
        Inventory inventory = inventoryRepository.findByProductIdAndLocationIdForUpdate(
                request.getProductId(), request.getLocationId())
            .orElseGet(() -> {
                Inventory newInventory = new Inventory();
                newInventory.setProduct(product);
                newInventory.setLocation(location);
                return inventoryRepository.save(newInventory);
            });

        int previousQuantity = inventory.getQuantity();
        int newQuantity = request.getNewQuantity();
        inventory.setQuantity(newQuantity);

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(product);
        transaction.setLocation(location);
        transaction.setTransactionType(TransactionType.ADJUSTMENT);
        transaction.setQuantity(Math.abs(newQuantity - previousQuantity));
        transaction.setPreviousQuantity(previousQuantity);
        transaction.setNewQuantity(newQuantity);
        transaction.setReason(request.getReason());
        transaction.setPerformedBy(getCurrentUser());

        inventoryRepository.save(inventory);
        InventoryTransaction record = inventoryTransactionRepository.save(transaction);

        return toResponse(record);
    }

    @Transactional
    public List<InventoryTransactionResponse> transfer(StockTransferRequest request) {
        // Make sure the product exists
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"));

        // Make sure the origin exists
        Location fromLocation = locationRepository.findById(request.getFromLocationId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source location not found"));

        // Make sure the destination exists
        Location toLocation = locationRepository.findById(request.getToLocationId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Destination location not found"));

        // Make sure the origin and destination are not the same
        if (request.getFromLocationId().equals(request.getToLocationId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Source and destination locations must be different");
        }

        // Lock the lower location id first, so two transfers cannot lock the same rows in opposite orders.
        Long firstLocationId = Math.min(request.getFromLocationId(), request.getToLocationId());
        Long secondLocationId = Math.max(request.getFromLocationId(), request.getToLocationId());
        Optional<Inventory> firstRow = inventoryRepository.findByProductIdAndLocationIdForUpdate(
                request.getProductId(), firstLocationId);
        Optional<Inventory> secondRow = inventoryRepository.findByProductIdAndLocationIdForUpdate(
                request.getProductId(), secondLocationId);

        boolean sourceIsFirst = request.getFromLocationId().equals(firstLocationId);
        Optional<Inventory> sourceRow = sourceIsFirst ? firstRow : secondRow;
        Optional<Inventory> destinationRow = sourceIsFirst ? secondRow : firstRow;

        Inventory sourceInventory = sourceRow
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "No inventory found for this product at the source location"));

        // Make sure source inventory has enough stock
        int sourcePreviousQuantity = sourceInventory.getQuantity();
        if (sourcePreviousQuantity < request.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock");
        }

        // Update source inventory to reflect quantity change
        int sourceNewQuantity = sourcePreviousQuantity - request.getQuantity();
        sourceInventory.setQuantity(sourceNewQuantity);

        // Create the destination row only after the source has enough stock.
        Inventory destinationInventory = destinationRow.orElseGet(() -> {
            Inventory newInventory = new Inventory();
            newInventory.setProduct(product);
            newInventory.setLocation(toLocation);
            return inventoryRepository.save(newInventory);
        });

        // Update destination inventory to reflect quantity change
        int destinationPreviousQuantity = destinationInventory.getQuantity();
        int destinationNewQuantity = destinationPreviousQuantity + request.getQuantity();
        destinationInventory.setQuantity(destinationNewQuantity);

        User currentUser = getCurrentUser();

        // Build the transfer-out transaction record
        InventoryTransaction transferOut = new InventoryTransaction();
        transferOut.setProduct(product);
        transferOut.setLocation(fromLocation);
        transferOut.setTransactionType(TransactionType.TRANSFER_OUT);
        transferOut.setQuantity(request.getQuantity());
        transferOut.setPreviousQuantity(sourcePreviousQuantity);
        transferOut.setNewQuantity(sourceNewQuantity);
        transferOut.setReason(request.getReason());
        transferOut.setPerformedBy(currentUser);

        // Build the transfer-in transaction record
        InventoryTransaction transferIn = new InventoryTransaction();
        transferIn.setProduct(product);
        transferIn.setLocation(toLocation);
        transferIn.setTransactionType(TransactionType.TRANSFER_IN);
        transferIn.setQuantity(request.getQuantity());
        transferIn.setPreviousQuantity(destinationPreviousQuantity);
        transferIn.setNewQuantity(destinationNewQuantity);
        transferIn.setReason(request.getReason());
        transferIn.setPerformedBy(currentUser);

        // Save both inventories
        inventoryRepository.save(sourceInventory);
        inventoryRepository.save(destinationInventory);

        // Save both transactions (link them together before saving) 
        transferOut = inventoryTransactionRepository.save(transferOut);
        transferIn.setRelatedTransaction(transferOut);
        transferIn = inventoryTransactionRepository.save(transferIn);
        transferOut.setRelatedTransaction(transferIn);
        transferOut = inventoryTransactionRepository.save(transferOut);

        // Return both transaction responses
        return List.of(toResponse(transferOut), toResponse(transferIn));
    }
}
