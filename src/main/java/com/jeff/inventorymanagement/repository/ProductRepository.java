package com.jeff.inventorymanagement.repository;

import com.jeff.inventorymanagement.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySku(String sku);

    @Query(value = """
        SELECT p.* FROM products p
        WHERE (:search IS NULL OR
               unaccent(LOWER(p.name)) LIKE '%' || unaccent(LOWER(:search)) || '%' OR
               unaccent(LOWER(p.sku)) LIKE '%' || unaccent(LOWER(:search)) || '%' OR
               unaccent(LOWER(p.description)) LIKE '%' || unaccent(LOWER(:search)) || '%')
          AND (:categoryId IS NULL OR p.category_id = :categoryId)
          AND (:supplierId IS NULL OR p.supplier_id = :supplierId)
          AND (:active IS NULL OR p.active = :active)
        """,
        countQuery = """
        SELECT COUNT(*) FROM products p
        WHERE (:search IS NULL OR
               unaccent(LOWER(p.name)) LIKE '%' || unaccent(LOWER(:search)) || '%' OR
               unaccent(LOWER(p.sku)) LIKE '%' || unaccent(LOWER(:search)) || '%' OR
               unaccent(LOWER(p.description)) LIKE '%' || unaccent(LOWER(:search)) || '%')
          AND (:categoryId IS NULL OR p.category_id = :categoryId)
          AND (:supplierId IS NULL OR p.supplier_id = :supplierId)
          AND (:active IS NULL OR p.active = :active)
        """,
        nativeQuery = true)
    Page<Product> findFiltered(String search, Long categoryId, Long supplierId, Boolean active, Pageable pageable);
}