package com.ecommerce.fitgearback.repositories;

import com.ecommerce.fitgearback.models.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorRepository extends JpaRepository <Vendor, Long>{
}
