package com.jeff.inventorymanagement.repository;

import com.jeff.inventorymanagement.entity.InventoryTransaction;
import com.jeff.inventorymanagement.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    @Query("""
        SELECT t FROM InventoryTransaction t
        WHERE (:productId IS NULL OR t.product.id = :productId)
          AND (:locationId IS NULL OR t.location.id = :locationId)
          AND (:transactionType IS NULL OR t.transactionType = :transactionType)
        """)
    Page<InventoryTransaction> findFiltered(
            Long productId, Long locationId, TransactionType transactionType, Pageable pageable);
}
