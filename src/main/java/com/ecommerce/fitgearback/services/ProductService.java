package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.dtos.product.ProductResponse;
import com.ecommerce.fitgearback.dtos.product.ProductRequest;
import com.ecommerce.fitgearback.mappers.ProductMapper;
import com.ecommerce.fitgearback.models.Product;
import com.ecommerce.fitgearback.repositories.CategoryRepository;
import com.ecommerce.fitgearback.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@AllArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public List<ProductResponse> getAllProducts(Long categoryId) {
        List<Product> products;
        if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId);
        } else {
            products = productRepository.findAllWithCategory();
        }

        return products.stream()
                .map(productMapper::toProductResponse).toList();
    }

    public ProductResponse getProductById(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return productMapper.toProductResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(@NotNull ProductRequest productRequest, MultipartFile image) {
        var category = categoryRepository
                .findById(productRequest.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));

        var product = productMapper.toProduct(productRequest);
        product.setCategory(category);

        if (image != null && !image.isEmpty()) {
            try {
                byte[] imageData = image.getBytes();
                product.setImageData(imageData);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read image", e);
            }
        }

        productRepository.save(product);
        var productResponse = productMapper.toProductResponse(product);
        productResponse.setId(product.getId());
        return productResponse;
    }

    @Transactional
    public ProductResponse updateProduct(@NotNull Long id,@NotNull ProductRequest productRequest) {
        var category = categoryRepository
                .findById(productRequest.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));

        var product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalStateException("Product not found"));

        productMapper.update(productRequest, product);
        product.setCategory(category);
        productRepository.save(product);
        var productResponse = productMapper.toProductResponse(product);
        productResponse.setId(product.getId());
        return productResponse;
    }

    @Transactional
    public void deleteProduct (Long id) {
        if (!productRepository.existsById(id)) {
            throw new NoSuchElementException("Product not found");
        }
        productRepository.deleteById(id);
    }

    public List<ProductResponse> getAllSorted(@NotNull String sortBy) {
        List<Product> products = new ArrayList<>();

        switch (sortBy) {
            case "price_asc" -> products = productRepository.findAllByOrderByPriceAsc();
            case "price_desc" -> products = productRepository.findAllByOrderByPriceDesc();
            case "stock_asc" -> products = productRepository.findAllByOrderByStockAsc();
            case "stock_desc" -> products = productRepository.findAllByOrderByStockDesc();
            case "name_asc" -> products = productRepository.findAllByOrderByNameAsc();
            case "name_desc" -> products = productRepository.findAllByOrderByNameDesc();
            default -> products = productRepository.findAll();
        }

        return products.stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    public Product getProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found with ID: " + id));
    }


}
