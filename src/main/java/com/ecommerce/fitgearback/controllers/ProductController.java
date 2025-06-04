package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.product.ProductRequest;
import com.ecommerce.fitgearback.dtos.product.ProductResponse;
import com.ecommerce.fitgearback.exceptions.ProductNotFoundException;
import com.ecommerce.fitgearback.models.Product;
import com.ecommerce.fitgearback.services.ProductService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("permitAll()")
    @GetMapping("/category/{id}")
    public ResponseEntity<Page<ProductResponse>> getAllProductsByCategory(
            @PathVariable("id") Long categoryId, Pageable pageable) {
        Page<ProductResponse> products = productService.getAllProductsByCategory(categoryId, pageable);

        return ResponseEntity.ok(products);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<ProductResponse>> getAllProducts(Pageable pageable) {
        Page<ProductResponse> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<Page<ProductResponse>> getProductsByVendorId(
            @PathVariable Long vendorId, Pageable pageable) {
        Page<ProductResponse> products = productService.getAllProductsByVendor(vendorId, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam String keyword, Pageable pageable) {
        Page<ProductResponse> response = productService.searchProducts(keyword, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getProductImage(@PathVariable Long id) {
        Product product = productService.getProductEntityById(id);
        if (product.getImageName() == null) {
            throw new ProductNotFoundException("Image not found for product ID: " + id);
        }
        Resource resource = new PathResource(Paths.get("Uploads", product.getImageName()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, product.getImageType() != null ? product.getImageType() : "image/jpeg")
                .body(resource);
    }

    @PreAuthorize("hasRole('VENDOR')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> createProduct(
            @ModelAttribute @Valid ProductRequest productRequest,
            UriComponentsBuilder uriBuilder) {
        ProductResponse createdProduct = productService.createProduct(productRequest);
        var uri = uriBuilder
                .path("/api/v1/products/{id}")
                .buildAndExpand(createdProduct.getId())
                .toUri();
        return ResponseEntity.created(uri).body(createdProduct);
    }

    @PreAuthorize("hasRole('VENDOR')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @ModelAttribute ProductRequest productRequest,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        ProductResponse updatedProduct = productService.updateProduct(id, productRequest, image);
        return ResponseEntity.ok(updatedProduct);
    }

    @PreAuthorize("hasRole('VENDOR')")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sort")
    public ResponseEntity<Page<ProductResponse>> getSortedProducts(
            @RequestParam(defaultValue = "") String sortBy, Pageable pageable) {
        Page<ProductResponse> sorted = productService.getAllSorted(sortBy, pageable);
        return ResponseEntity.ok(sorted);
    }


    @PreAuthorize("permitAll()")
    @GetMapping("/filter")
    public ResponseEntity<Page<ProductResponse>> filterProducts(
            @RequestParam(required = false) String priceRange, // e.g., "10-50"
            @RequestParam(required = false) Long categoryId,
            Pageable pageable) {
        // Build filter parameters dynamically
        Map<String, Object> filters = new HashMap<>();
        if (priceRange != null && !priceRange.isEmpty()) {
            filters.put("priceRange", priceRange);
        }
        if (categoryId != null) {
            filters.put("categoryId", categoryId);
        }

        // Pass filters to service layer
        Page<ProductResponse> products = productService.filterProducts(filters, pageable);
        return ResponseEntity.ok(products);
    }
}