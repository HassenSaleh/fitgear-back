package com.ecommerce.fitgearback.repositories;

import com.ecommerce.fitgearback.models.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart,Long> {
    @EntityGraph(attributePaths = "items.product")
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.product WHERE c.id = :cartId")
    Optional<Cart> getCartWithItems(@Param("cartId") Long cartId);


    @EntityGraph(attributePaths = {"items.product"})
    Optional<Cart> findByCustomerId(Long customerId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.product WHERE c.id = :id")
    Optional<Cart> findByIdWithItemsFetched(@Param("id") Long id);
}
