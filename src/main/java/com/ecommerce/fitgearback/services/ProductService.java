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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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

    public List<ProductResponse> getProductByCategory(Long categoryId){
        List<Product> product = productRepository.findByCategoryId(categoryId);
        return productMapper.toProductResponseList(product);
    }

    public List<ProductResponse> getProductByVendor(Long id){
        Optional<Product> product = productRepository.getProductByVendorId(id);
        return productMapper.toProductResponseList(product.stream().toList());
    }

    public List<ProductResponse> searchProduct(String search){
        List<Product> products = productRepository.searchAllBy(search);
        return productMapper.toProductResponseList(products);
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

                String imageName = "product-" + UUID.randomUUID() + "." + getExtension(image.getOriginalFilename());
                Path imagePath = Paths.get("uploads", imageName);
                Files.copy(image.getInputStream(), imagePath);

                product.setImageName(imageName);
                product.setImageType(image.getContentType());
                product.setImageData(null);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read image", e);
            }
        }

        productRepository.save(product);
        var productResponse = productMapper.toProductResponse(product);
        productResponse.setImageUrl("/uploads/" + product.getImageName());
        productResponse.setId(product.getId());
        return productResponse;
    }

    @Transactional
    public ProductResponse updateProduct(
            @NotNull Long id,
            @NotNull ProductRequest productRequest,
            MultipartFile image) {
        var category = categoryRepository
                .findById(productRequest.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));

        var product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Product not found"));

        productMapper.update(productRequest, product);
        product.setCategory(category);

        if (image != null && !image.isEmpty()) {
            try {
                byte[] imageData = image.getBytes();
                product.setImageData(imageData);
                product.setImageType(image.getContentType());

                String imageName = "product-" + UUID.randomUUID() + "." + getExtension(image.getOriginalFilename());
                Path imagePath = Paths.get("uploads", imageName);
                Files.copy(image.getInputStream(), imagePath);
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
    public void deleteProduct (Long productId,Long categoryId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        if (!product.getVendor().getId().equals(categoryId)) {
            throw new AccessDeniedException("You are not authorized to delete this product.");
        }

        productRepository.delete(product);
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


    private String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf('.') + 1);
    }


}
