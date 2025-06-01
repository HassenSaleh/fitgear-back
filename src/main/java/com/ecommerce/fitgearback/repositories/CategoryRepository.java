package com.ecommerce.fitgearback.repositories;

import com.ecommerce.fitgearback.models.Category;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {

    @NotNull Optional<Category> findById(@NotNull Long categoryId);
    List<Category> findByNameContainingIgnoreCase(String name);
}
