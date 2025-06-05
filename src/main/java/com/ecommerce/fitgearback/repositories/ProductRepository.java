package com.ecommerce.fitgearback.repositories;

import com.ecommerce.fitgearback.models.Product;
import jakarta.persistence.LockModeType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = "category")
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId,Pageable pageable);

    @NotNull Page<Product> findAll(@NotNull Pageable pageable);

    @EntityGraph(attributePaths = "category")
    @Query("SELECT p FROM Product p WHERE p.category = :categorId")
    Page<Product> findAllWithCategory(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = :status AND " +
            "(p.brand IS NOT NULL AND LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(p.description IS NOT NULL AND LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(p.name IS NOT NULL AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchAllBy(@Param("keyword") String keyword,Pageable pageable);

    Long countByVendorId(Long vendorId);

    @NotNull Optional<Product> findById(@NotNull Long productId);

    @Query("SELECT p FROM Product p WHERE p.vendor.id = :vendorId")
    Page<Product> findByVendorId(@Param("vendorId") Long vendorId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findAllByIdWithLock(@Param("ids") Set<Long> ids);

    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    Page<Product> findByPriceBetweenAndCategoryId(BigDecimal minPrice, BigDecimal maxPrice, Long categoryId, Pageable pageable);
}