package com.jeff.inventorymanagement.repository;

import com.jeff.inventorymanagement.entity.InventoryTransaction;
import com.jeff.inventorymanagement.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    @Query("""
        SELECT t FROM InventoryTransaction t
        WHERE (:productId IS NULL OR t.product.id = :productId)
          AND (:locationId IS NULL OR t.location.id = :locationId)
          AND (:transactionType IS NULL OR t.transactionType = :transactionType)
          AND (CAST(:startDateTime AS LocalDateTime) IS NULL OR t.createdAt >= :startDateTime)
          AND (CAST(:endDateTime AS LocalDateTime) IS NULL OR t.createdAt < :endDateTime)
        """)
    Page<InventoryTransaction> findFiltered(
            Long productId, Long locationId, TransactionType transactionType,
            LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);
}
