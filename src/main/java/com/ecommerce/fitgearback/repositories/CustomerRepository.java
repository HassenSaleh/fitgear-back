package com.ecommerce.fitgearback.repositories;

import com.ecommerce.fitgearback.models.Customer;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("SELECT c FROM Customer c WHERE c.id = :customerId")
    Optional<Customer> getCustomerById(@Param("customerId") Long customerId);
    Optional<Customer> findByEmail(String email);
    @NotNull Optional<Customer> findById(@NotNull Long userId);


}
