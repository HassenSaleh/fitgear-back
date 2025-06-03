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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/")
    public ResponseEntity<List<ProductResponse>> getAllProductsByCategory(@RequestParam(required = false) Long categoryId) {
        List<ProductResponse> products = productService.getAllProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductResponse> products = productService.getAllProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category/vendor/{vendorId}")
    public ResponseEntity<List<ProductResponse>> getProductByVendorId(@PathVariable Long vendorId) {
        List<ProductResponse> products = productService.getAllProductsByVendor(vendorId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @RequestParam String keyword){
        var response = productService.searchProducts(keyword);
        if(response.isEmpty()){
            return ResponseEntity.notFound().build();
        }
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
    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> createProduct(
            @ModelAttribute ProductRequest productRequest,
            UriComponentsBuilder uriBuilder) {

        ProductResponse createdProduct = productService.createProduct(productRequest);

        var uri = uriBuilder
                .path("/api/v1/product/{id}")
                .buildAndExpand(createdProduct.getId())
                .toUri();

        return ResponseEntity.created(uri).body(createdProduct);
    }


    @PreAuthorize("hasRole('VENDOR')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestPart("product") ProductRequest productRequest,
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

    private static final Set<String> VALID_SORT_FIELDS = Set.of(
            "price_asc", "price_desc", "stock_asc", "stock_desc", "name_asc", "name_desc"
    );

    @GetMapping("/sorted")
    public ResponseEntity<List<ProductResponse>> getSortedProducts(
            @RequestParam(defaultValue = "") String sortBy) {
        if (!sortBy.isBlank() && !VALID_SORT_FIELDS.contains(sortBy.toLowerCase())) {
            throw new IllegalArgumentException("Invalid sort field: " + sortBy);
        }
        List<ProductResponse> sorted = productService.getAllSorted(sortBy);
        return ResponseEntity.ok(sorted);
    }
}