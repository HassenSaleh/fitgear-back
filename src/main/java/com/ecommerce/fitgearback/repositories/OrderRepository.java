package com.ecommerce.fitgearback.repositories;

import com.ecommerce.fitgearback.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("""
    SELECT COUNT(DISTINCT o.customer.id)
    FROM Order o
    JOIN o.items i
    JOIN i.product p
    WHERE p.vendor.id = :vendorId""")
    Long countDistinctCustomersByVendorId(@Param("vendorId") Long vendorId);


    @Query("""
        SELECT COUNT(DISTINCT o.id)
        FROM Order o
        JOIN o.items i
        JOIN i.product p
        WHERE p.vendor.id = :vendorId
    """)
    Long countOrdersByVendorId(@Param("vendorId") Long vendorId);

    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        JOIN o.items i
        JOIN i.product p
        WHERE p.vendor.id = :vendorId
    """)
    BigDecimal calculateTotalRevenueByVendorId(@Param("vendorId") Long vendorId);
}
