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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.file.Paths;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/all")
    public ResponseEntity<List<ProductResponse>> getAllProducts(@RequestParam(required = false) Long categoryId) {
        List<ProductResponse> products = productService.getAllProducts(categoryId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductResponse> products = productService.getAllProducts(categoryId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/search/{search}")
    public ResponseEntity<List<ProductResponse>> searchProduct(@PathVariable String search) {
        List<ProductResponse> products = productService.searchProduct(search);
        return ResponseEntity.ok(products);
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
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestPart("product") ProductRequest productRequest,
            @RequestPart(value = "image", required = false) MultipartFile image,
            UriComponentsBuilder uriBuilder) {
        ProductResponse createdProduct = productService.createProduct(productRequest, image);
        var uri = uriBuilder.path("/api/v1/product/{id}").buildAndExpand(createdProduct.getId()).toUri();
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

    @GetMapping("/sorted")
    public ResponseEntity<List<ProductResponse>> getSortedProducts(
            @RequestParam(defaultValue = "") String sortBy) {
        List<ProductResponse> sorted = productService.getAllSorted(sortBy);
        return ResponseEntity.ok(sorted);
    }
}