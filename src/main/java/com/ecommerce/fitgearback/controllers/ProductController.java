package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.ProductDto;
import com.ecommerce.fitgearback.mappers.ProductMapper;
import com.ecommerce.fitgearback.services.ProductService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping
    public ResponseEntity<ProductDto> getProductById(Long id){
        ProductDto product = productService.getProductById(id);
        if(product == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @GetMapping("{/categoryId}")
    public ResponseEntity<List<ProductDto>> getAllProducts(
            @PathVariable(name = "categoryId", required = false) Long categoryId
    ) {
        List<ProductDto> products = productService.getAllProducts(categoryId);

        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        var product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }


    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ProductDto> createProduct(
            @Valid @RequestPart("product") ProductDto productDto,
            @Valid @RequestPart(value = "image", required = false) MultipartFile image,
            UriComponentsBuilder uriBuilder) {
        try {
            ProductDto createdProduct = productService.createProduct(productDto, image);
            var uri = uriBuilder.path("/products/{id}")
                    .buildAndExpand(createdProduct.getId())
                    .toUri();
            return ResponseEntity.created(uri).body(createdProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDto productDto) {

        try {
            ProductDto updatedProduct = productService.updateProduct(id, productDto);
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
}
