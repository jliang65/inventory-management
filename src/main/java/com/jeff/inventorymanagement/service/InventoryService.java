package com.jeff.inventorymanagement.service;

import com.jeff.inventorymanagement.dto.InventoryRequest;
import com.jeff.inventorymanagement.dto.InventoryResponse;
import com.jeff.inventorymanagement.entity.Inventory;
import com.jeff.inventorymanagement.entity.Location;
import com.jeff.inventorymanagement.entity.Product;
import com.jeff.inventorymanagement.repository.InventoryRepository;
import com.jeff.inventorymanagement.repository.LocationRepository;
import com.jeff.inventorymanagement.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            ProductRepository productRepository,
                            LocationRepository locationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.locationRepository = locationRepository;
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
        response.setReorderLevel(inventory.getReorderLevel());
        return response;
    }

    public Inventory toEntity(InventoryRequest request) {
        Inventory inventory = new Inventory();
        
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"));
        inventory.setProduct(product);
        
        Location location = locationRepository.findById(request.getLocationId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Location not found"));
        inventory.setLocation(location);
        
        inventory.setQuantity(request.getQuantity());
        inventory.setReorderLevel(request.getReorderLevel());
        return inventory;
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

    public List<Inventory> findLowStock() {
        return inventoryRepository.findLowStock();
    }

    public List<InventoryResponse> findLowStockAsResponse() {
        return findLowStock().stream()
            .map(this::toResponse)
            .toList();
    }

    public InventoryResponse saveFromRequest(InventoryRequest request) {
        if (inventoryRepository.existsByProductIdAndLocationId(
                request.getProductId(), request.getLocationId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Inventory already exists for this product-location combination");
        }
        
        Inventory inventory = toEntity(request);
        return toResponse(inventoryRepository.save(inventory));
    }
}