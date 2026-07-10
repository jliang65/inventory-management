package com.jeff.inventorymanagement.repository;

import com.jeff.inventorymanagement.entity.InventoryTransaction;
import com.jeff.inventorymanagement.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    List<InventoryTransaction> findAllByOrderByCreatedAtDesc();

    List<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<InventoryTransaction> findByLocationIdOrderByCreatedAtDesc(Long locationId);

    List<InventoryTransaction> findByProductIdAndLocationIdOrderByCreatedAtDesc(Long productId, Long locationId);

    List<InventoryTransaction> findByTransactionTypeOrderByCreatedAtDesc(TransactionType transactionType);
}
