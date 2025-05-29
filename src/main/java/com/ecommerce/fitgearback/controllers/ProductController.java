package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.product.ProductRequest;
import com.ecommerce.fitgearback.dtos.product.ProductResponse;
import com.ecommerce.fitgearback.models.Product;
import com.ecommerce.fitgearback.services.ProductService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.NoSuchElementException;

@Setter
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/product")

public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(@RequestParam(required = false) Long id){
        List<ProductResponse> products = productService.getAllProducts(id);
        if(products == null || products.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductResponse> products = productService.getAllProducts(categoryId);
        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(products);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        var product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long id) {
        Product product = productService.getProductEntityById(id); // Return Product not DTO
        byte[] image = product.getImageData();

        if (image == null || image.length == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, product.getImageType() != null ? product.getImageType() : "image/jpeg")
                .body(image);
    }




    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestPart("product") ProductRequest productRequest,
            @Valid @RequestPart(value = "image", required = false) MultipartFile image,
            UriComponentsBuilder uriBuilder) {
        try {
            ProductResponse createdProduct = productService.createProduct(productRequest, image);
            var uri = uriBuilder
                    .path("/products/{id}")
                    .buildAndExpand(createdProduct.getId())
                    .toUri();
            return ResponseEntity.created(uri).body(createdProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest productRequest) {

        try {
            ProductResponse updatedProduct = productService.updateProduct(id, productRequest);
            return ResponseEntity.ok(updatedProduct);
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/sorted")
    public ResponseEntity<List<ProductResponse>> getSortedProducts(
            @RequestParam(defaultValue = "") String sortBy
    ) {
        List<ProductResponse> sorted = productService.getAllSorted(sortBy);
        return sorted.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(sorted);
    }

}
