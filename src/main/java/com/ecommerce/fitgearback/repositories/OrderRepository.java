package com.ecommerce.fitgearback.repositories;

import com.ecommerce.fitgearback.models.Customer;
import com.ecommerce.fitgearback.models.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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

    @EntityGraph(attributePaths = "items.product")
    @Query("SELECT o FROM Order o WHERE o.customer = :customer")
    List<Order> getOrdersByCustomer(@Param("customer") Customer customer);

    @EntityGraph(attributePaths = "items.product")
    @Query("SELECT o FROM Order o WHERE o.id = :orderId")
    Optional<Order> getOrderWithItems(@Param("orderId") Long orderId);

}
