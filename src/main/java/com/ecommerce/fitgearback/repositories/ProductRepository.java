package com.ecommerce.fitgearback.repositories;

import com.ecommerce.fitgearback.models.Product;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = "category")
    List<Product> findByCategoryId(Long categoryId);
    @EntityGraph(attributePaths = "category")
    @Query("SELECT p FROM Product p")
    List<Product> findAllWithCategory();

    @Query("SELECT p FROM Product p WHERE " +
            "(p.brand IS NOT NULL AND LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(p.description IS NOT NULL AND LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
            "(p.name IS NOT NULL AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchAllBy(@Param("keyword") String keyword);
    Long countByVendorId(Long vendor_id);
    @NotNull Optional<Product> findById(@NotNull Long productId);
    List<Product> findByVendorId(Long vendorId);
}
