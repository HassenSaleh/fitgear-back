package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.category.CategoryRequest;
import com.ecommerce.fitgearback.dtos.category.CategoryResponse;
import com.ecommerce.fitgearback.services.CategoryService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("permitAll()")
@AllArgsConstructor
@Data
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
   private final CategoryService categoryService;

   @PreAuthorize("permitAll()")
   @PostMapping("/")
   public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest dto) {
      return ResponseEntity.ok(categoryService.createCategory(dto));
   }

   @PreAuthorize("permitAll()")
   @GetMapping("/")
   public ResponseEntity<List<CategoryResponse>> getAllCategories() {
      return ResponseEntity.ok(categoryService.getAllCategories());
   }

   @PreAuthorize("permitAll()")
   @GetMapping("/{id}")
   public ResponseEntity<CategoryResponse> getCategory(@PathVariable Long id) {
      return ResponseEntity.ok(categoryService.getCategoryById(id));
   }

   @PreAuthorize("permitAll()")
   @GetMapping("/{name}")
   public ResponseEntity<List<CategoryResponse>> getCategories(@PathVariable (required = false) String name) {
      return ResponseEntity.ok(categoryService.getCategories(name));
   }

   @PreAuthorize("permitAll()")
   @PutMapping("/{id}")
   public ResponseEntity<CategoryResponse> updateCategory(
           @PathVariable Long id,
           @RequestBody CategoryRequest dto) {
      return ResponseEntity.ok(categoryService.updateCategory(id, dto));
   }

   @PreAuthorize("permitAll()")
   @DeleteMapping("/{id}")
   public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
      categoryService.deleteCategory(id);
      return ResponseEntity.noContent().build();
   }

}
