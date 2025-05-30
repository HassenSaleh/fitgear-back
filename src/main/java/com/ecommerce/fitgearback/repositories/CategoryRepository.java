package com.ecommerce.fitgearback.repositories;

import com.ecommerce.fitgearback.models.Category;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {

    @NotNull Optional<Category> findById(Long categoryId);
}
