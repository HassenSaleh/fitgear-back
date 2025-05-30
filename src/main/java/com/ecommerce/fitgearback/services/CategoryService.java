package com.ecommerce.fitgearback.services;


import com.ecommerce.fitgearback.dtos.category.CategoryRequest;
import com.ecommerce.fitgearback.dtos.category.CategoryResponse;
import com.ecommerce.fitgearback.mappers.CategoryMapper;
import com.ecommerce.fitgearback.models.Category;
import com.ecommerce.fitgearback.repositories.CategoryRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Data
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;


    public CategoryResponse createCategory(CategoryRequest dto) {
        Category category = categoryMapper.toCategory(dto);
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }


    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return categoryMapper.toCategoryResponse(category);
    }

    public CategoryResponse updateCategory(Long id, @NotNull CategoryRequest dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found");
        }
        categoryRepository.deleteById(id);
    }

    public List<CategoryResponse> getCategories(String name) {
        List<Category> categories;
        if (name != null && !name.isEmpty()) {
            categories = categoryRepository.findByNameContainingIgnoreCase(name);
        } else {
            categories = categoryRepository.findAll();
        }
        return categories.stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

}
