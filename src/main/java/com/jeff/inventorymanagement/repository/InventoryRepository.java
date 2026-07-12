package com.jeff.inventorymanagement.repository;

import com.jeff.inventorymanagement.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByProductId(Long productId);
    List<Inventory> findByLocationId(Long locationId);
    boolean existsByProductIdAndLocationId(Long productId, Long locationId);
    Optional<Inventory> findByProductIdAndLocationId(Long productId, Long locationId);

    @Query("""
        SELECT i FROM Inventory i
        WHERE (:productId IS NULL OR i.product.id = :productId)
          AND (:locationId IS NULL OR i.location.id = :locationId)
        """)
    Page<Inventory> findFiltered(Long productId, Long locationId, Pageable pageable);

    @Query("""
        SELECT i
        FROM Inventory i
        WHERE i.quantity <= i.reorderLevel
    """)
    List<Inventory> findLowStock();
}