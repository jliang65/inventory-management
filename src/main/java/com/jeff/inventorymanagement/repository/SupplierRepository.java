package com.jeff.inventorymanagement.repository;

import com.jeff.inventorymanagement.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    boolean existsByName(String name);
}