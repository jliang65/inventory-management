package com.jeff.inventorymanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PurchaseOrderItemUpdateRequest {

    @NotNull(message = "Ordered quantity is required")
    @Min(value = 1, message = "Ordered quantity must be at least 1")
    private Integer orderedQuantity;

    @NotNull(message = "Unit cost is required")
    @Min(value = 0, message = "Unit cost cannot be negative")
    private BigDecimal unitCost;

    public Integer getOrderedQuantity() {
        return orderedQuantity;
    }

    public void setOrderedQuantity(Integer orderedQuantity) {
        this.orderedQuantity = orderedQuantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }
}
