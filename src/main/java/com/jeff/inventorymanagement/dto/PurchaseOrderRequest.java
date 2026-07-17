package com.jeff.inventorymanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class PurchaseOrderRequest {

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotNull(message = "Destination location ID is required")
    private Long destinationLocationId;

    private String notes;

    @Valid
    private List<PurchaseOrderItemRequest> items;

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public Long getDestinationLocationId() {
        return destinationLocationId;
    }

    public void setDestinationLocationId(Long destinationLocationId) {
        this.destinationLocationId = destinationLocationId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<PurchaseOrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<PurchaseOrderItemRequest> items) {
        this.items = items;
    }
}
