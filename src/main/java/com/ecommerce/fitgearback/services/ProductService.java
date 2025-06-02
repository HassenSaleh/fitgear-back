package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.dtos.product.ProductRequest;
import com.ecommerce.fitgearback.dtos.product.ProductResponse;
import com.ecommerce.fitgearback.enums.ProductStatus;
import com.ecommerce.fitgearback.exceptions.ImageProcessingException;
import com.ecommerce.fitgearback.exceptions.ProductNotFoundException;
import com.ecommerce.fitgearback.mappers.ProductMapper;
import com.ecommerce.fitgearback.models.Category;
import com.ecommerce.fitgearback.models.Product;
import com.ecommerce.fitgearback.models.Vendor;
import com.ecommerce.fitgearback.repositories.CategoryRepository;
import com.ecommerce.fitgearback.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final AuthService authService;
    private static final String UPLOAD_DIR = "Uploads/";

    public List<ProductResponse> getAllProductsByCategory(Long categoryId) {
        List<Product> products = categoryId != null
                ? productRepository.findByCategoryId(categoryId)
                : productRepository.findAllWithCategory();
        return productMapper.toProductResponseList(products);
    }

    public List<ProductResponse> getAllProductsByVendor(Long vendorId) {
        List<Product> products = vendorId != null
                ? productRepository.findByVendorId(vendorId)
                : productRepository.findAllWithCategory();
        return productMapper.toProductResponseList(products);
    }
    public List<ProductResponse> getAllProducts(){
        List<Product> products = productRepository.findAllWithCategory();
        if(products.isEmpty()) {
            return null;
        }
        return productMapper.toProductResponseList(products);
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
        return productMapper.toProductResponse(product);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.searchAllBy(keyword);
    }

    public ProductResponse createProduct(ProductRequest request) {
        Vendor vendor = authService.getCurrentVendor();
        if (vendor == null) {
            throw new AccessDeniedException("Authenticated user is not a vendor");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.getCategoryId()));

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setBrand(request.getBrand());
        product.setCategory(category);
        product.setVendor(vendor); // Set vendor from authenticated user
        product.setActive(request.getStatus() != null ? request.getStatus() : ProductStatus.ACTIVE);
        product.setCreatedAt(LocalDateTime.now());

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                String fileName = UUID.randomUUID() + "_" + request.getImage().getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR, fileName);
                Files.createDirectories(filePath.getParent());
                request.getImage().transferTo(filePath);
                product.setImageName(fileName);
                product.setImageType(request.getImage().getContentType());
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }
        Product savedProduct = productRepository.save(product);
        return productMapper.toProductResponse(savedProduct);
    }



    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest productRequest, MultipartFile image) {
        Vendor vendor = authService.getCurrentVendor();
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        if (!product.getVendor().getId().equals(vendor.getId())) {
            throw new AccessDeniedException("You can only update your own products");
        }

        var category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + productRequest.getCategoryId()));

        validateProductRequest(productRequest);

        productMapper.update(productRequest, product);
        product.setCategory(category);

        if (image != null && !image.isEmpty()) {
            try {
                deleteImage(product.getImageName());
                String imageName = saveImage(image);
                product.setImageName(imageName);
                product.setImageType(image.getContentType());
            } catch (IOException e) {
                logger.error("Failed to update image for product ID: {}", id, e);
                throw new ImageProcessingException("Failed to update image", e);
            }
        }

        productRepository.save(product);
        ProductResponse response = productMapper.toProductResponse(product);
        if (product.getImageName() != null) {
            response.setImageUrl("/Uploads/" + product.getImageName());
        }
        return response;
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Vendor vendor = authService.getCurrentVendor();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        if (!product.getVendor().getId().equals(vendor.getId())) {
            throw new AccessDeniedException("You can only delete your own products");
        }

        deleteImage(product.getImageName());
        productRepository.delete(product);
    }

    public List<ProductResponse> getAllSorted(String sortBy) {
        List<Product> products = switch (sortBy.toLowerCase()) {
            case "price_asc" -> productRepository.findAllByOrderByPriceAsc();
            case "price_desc" -> productRepository.findAllByOrderByPriceDesc();
            case "stock_asc" -> productRepository.findAllByOrderByStockAsc();
            case "stock_desc" -> productRepository.findAllByOrderByStockDesc();
            case "name_asc" -> productRepository.findAllByOrderByNameAsc();
            case "name_desc" -> productRepository.findAllByOrderByNameDesc();
            default -> productRepository.findAll();
        };
        return productMapper.toProductResponseList(products);
    }

    public Product getProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
    }

    private void validateProductRequest(ProductRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
        if (request.getStock() == null || request.getStock() < 0) {
            throw new IllegalArgumentException("Stock must be non-negative");
        }
    }

    private String saveImage(MultipartFile image) throws IOException {
        String extension = getExtension(image.getOriginalFilename());
        if (!List.of("jpg", "jpeg", "png", "gif").contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid image format: " + extension);
        }
        if (image.getSize() > 5 * 1024 * 8 * 1024) { // 5MB limit
            throw new IllegalArgumentException("Image size exceeds 5MB");
        }

        String imageName = "product-" + UUID.randomUUID() + "." + extension;
        Path uploadDir = Paths.get("Uploads");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        Path imagePath = uploadDir.resolve(imageName);
        Files.copy(image.getInputStream(), imagePath);
        return imageName;
    }

    private void deleteImage(String imageName) {
        if (imageName != null) {
            try {
                Path imagePath = Paths.get("Uploads", imageName);
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                logger.warn("Failed to delete image: {}", imageName, e);
            }
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}