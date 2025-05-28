package com.ecommerce.fitgearback.repositories;

import com.ecommerce.fitgearback.models.Admin;
import com.ecommerce.fitgearback.models.Vendor;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin,Long> {
    Optional<Admin> findByEmail(String email);
    boolean existsByEmail(String email);
}
