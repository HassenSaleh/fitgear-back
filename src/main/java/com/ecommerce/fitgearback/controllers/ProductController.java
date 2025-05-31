package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.product.ProductRequest;
import com.ecommerce.fitgearback.dtos.product.ProductResponse;
import com.ecommerce.fitgearback.models.Product;
import com.ecommerce.fitgearback.services.ProductService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173" , allowCredentials = "true")

@Setter
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/product")

public class ProductController {
    private final ProductService productService;

    @GetMapping("/all")
    public ResponseEntity<List<ProductResponse>> getAllProducts(@RequestParam(required = false) Long id){
        List<ProductResponse> products = productService.getAllProducts(id);
        if(products == null || products.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}")
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

    @GetMapping("/search/{search}")
    public ResponseEntity<List<ProductResponse>> searchProduct(@PathVariable String search) {
        List<ProductResponse> products = productService.searchProduct(search);
        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(products);
    }


    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getProductImage(@PathVariable Long id) {
        Product product = productService.getProductEntityById(id);
        byte[] image = product.getImageData();

        if (image == null || image.length == 0) {
            return ResponseEntity.notFound().build();
        }

        // Wrap byte[] in ByteArrayResource
        ByteArrayResource resource = new ByteArrayResource(image);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, product.getImageType() != null ? product.getImageType() : "image/jpeg")
                .body(resource);
    }



    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestPart("product") ProductRequest productRequest,
            @Valid @RequestPart(value = "image", required = false) MultipartFile image,
            UriComponentsBuilder uriBuilder) {
        try {
            ProductResponse createdProduct = productService.createProduct(productRequest, image);
            var uri = uriBuilder
                    .path("/api/v1/product/{id}")
                    .buildAndExpand(createdProduct.getId())
                    .toUri();
            return ResponseEntity.created(uri).body(createdProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestPart("product") ProductRequest productRequest,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        try {
            ProductResponse updatedProduct = productService.updateProduct(id, productRequest, image);
            return ResponseEntity.ok(updatedProduct);
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/vendor/{vendorId}/product/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId, @PathVariable Long vendorId) {
        try {
            productService.deleteProduct(productId, vendorId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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
