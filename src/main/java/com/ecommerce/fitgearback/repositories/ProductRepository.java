package com.ecommerce.fitgearback.repositories;

import com.ecommerce.fitgearback.dtos.product.ProductResponse;
import com.ecommerce.fitgearback.models.Product;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
            " LOWER(p.brand) LIKE LOWER(CONCAT('%', ':keyword', '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%',':keyword','%')) OR "+
            "LOWER(p.name) LIKE(CONCAT('%',':keyword','%'))"
    )
    List<Product> searchAllBy(String keyword);
    List<Product> findAllByOrderByPriceAsc();
    List<Product> findAllByOrderByPriceDesc();
    List<Product> findAllByOrderByStockAsc();
    List<Product> findAllByOrderByStockDesc();
    List<Product> findAllByOrderByNameAsc();
    List<Product> findAllByOrderByNameDesc();
    Long countByVendorId(Long vendor_id);
    @NotNull Optional<Product> findById(@NotNull Long productId);
    List<Product> findByVendorId(Long vendorId);

    List<Product> getAllProducts();

    //List<ProductResponse> getAllProducts();
}
