package com.ecommerce.fitgearback.repositories;

import com.ecommerce.fitgearback.models.Order;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
        SELECT COUNT(DISTINCT o.customer.id)
        FROM Order o
        JOIN o.items i
        JOIN i.product p
        WHERE p.vendor.id = :vendorId
        """)
    Long countDistinctCustomersByVendorId(@Param("vendorId") Long vendorId);

    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        JOIN o.items i
        JOIN i.product p
        WHERE p.vendor.id = :vendorId
        """)
    BigDecimal calculateTotalRevenueByVendorId(@Param("vendorId") Long vendorId);

    @EntityGraph(attributePaths = {"items.product"})
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId")
    Page<Order> findByCustomerIdWithItems(@Param("customerId") Long customerId, Pageable pageable);

    @EntityGraph(attributePaths = {"items.product"})
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items LEFT JOIN FETCH o.customer WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @EntityGraph(attributePaths = {"items.product"})
    @Query("SELECT o FROM Order o JOIN o.items i WHERE i.product.vendor.id = :vendorId")
    Page<Order> findByProductsVendorId(@Param("vendorId") Long vendorId, Pageable pageable);

    @Query("""
        SELECT COUNT(DISTINCT o.id)
        FROM Order o
        JOIN o.items i
        JOIN i.product p
        WHERE p.vendor.id = :vendorId
        """)
    @QueryHints(@QueryHint(name = "org.hibernate.comment", value = "countOrdersByVendorId"))
    Long countOrdersByVendorId(@Param("vendorId") Long vendorId);

    Optional<Order> findByTxRef(String txRef);
}