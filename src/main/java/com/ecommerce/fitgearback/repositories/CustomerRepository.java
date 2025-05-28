package com.ecommerce.fitgearback.repositories;

import com.ecommerce.fitgearback.models.Customer;
import com.ecommerce.fitgearback.models.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {
    Optional<Customer> findByEmail(String email);
}
