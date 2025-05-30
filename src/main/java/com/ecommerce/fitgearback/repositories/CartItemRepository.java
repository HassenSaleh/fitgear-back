package com.ecommerce.fitgearback.repositories;

import com.ecommerce.fitgearback.models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Long> {
    List<CartItem> findByCartCustomerId(Long customerId);;
}
