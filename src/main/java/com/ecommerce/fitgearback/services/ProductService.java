package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.dtos.ProductDto;
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
import java.util.List;
import java.util.NoSuchElementException;

@AllArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

//    public Product saveProduct(ProductDto dto){
//        return productRepository.save(dto.toProduct());
//    }
    public Iterable<Product> getProduct(){
        return productRepository.findAll();
    }

    public List<Product> findByCategoryId(Long id){
        return productRepository.findByCategoryId(id);
    }
    public List<ProductDto> getAllProducts(Long categoryId) {
        List<Product> products;
        if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId);
        } else {
            products = productRepository.findAllWithCategory();
        }

        return products.stream()
                .map(productMapper::toDto).toList();
    }

    public ProductDto getProductById(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return productMapper.toDto(product);
    }

    @Transactional
    public ProductDto createProduct(@NotNull ProductDto productDto,MultipartFile image) {
        var category = categoryRepository
                .findById(productDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));

        var product = productMapper.toEntity(productDto);
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
        productDto.setId(product.getId());
        return productDto;
    }

    @Transactional
    public ProductDto updateProduct(@NotNull Long id,@NotNull ProductDto productDto) {
        var category = categoryRepository
                .findById(productDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));

        var product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalStateException("Product not found"));

        productMapper.update(productDto, product);
        product.setCategory(category);
        productRepository.save(product);

        productDto.setId(product.getId());
        return productDto;
    }

    @Transactional
    public void deleteProduct (Long id) {
        if (!productRepository.existsById(id)) {
            throw new NoSuchElementException("Product not found");
        }
        productRepository.deleteById(id);
    }
}
