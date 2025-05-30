package com.ecommerce.fitgearback.repositories;

import com.ecommerce.fitgearback.models.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorRepository extends JpaRepository <Vendor, Long>{
    Optional<Vendor> findByEmail(String email);
}
