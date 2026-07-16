package com.jeff.inventorymanagement.repository;

import com.jeff.inventorymanagement.entity.PurchaseOrder;
import com.jeff.inventorymanagement.entity.PurchaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query("""
        SELECT po FROM PurchaseOrder po
        WHERE (:supplierId IS NULL OR po.supplier.id = :supplierId)
          AND (:destinationLocationId IS NULL OR po.destinationLocation.id = :destinationLocationId)
          AND (:status IS NULL OR po.status = :status)
        """)
    Page<PurchaseOrder> findFiltered(
            Long supplierId, Long destinationLocationId, PurchaseOrderStatus status, Pageable pageable);
}
