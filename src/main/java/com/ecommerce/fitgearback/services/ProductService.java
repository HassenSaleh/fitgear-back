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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
import java.util.*;

@AllArgsConstructor
@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private static final String UPLOAD_DIR = "Uploads/";
    private static final Set<String> VALID_SORT_FIELDS = Set.of(
            "price_asc", "price_desc", "stock_asc", "stock_desc", "name_asc", "name_desc"
    );

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final AuthService authService;

    public Page<ProductResponse> getAllProductsByCategory(Long categoryId, Pageable pageable) {

        if (categoryId != null && !categoryRepository.existsById(categoryId)) {
            logger.warn("Category ID {} does not exist", categoryId);
            throw new IllegalArgumentException("Category not found with ID: " + categoryId);
        }
        Page<Product> products = categoryId != null
                ? productRepository.findByCategoryId(categoryId, pageable)
                : productRepository.findAll(pageable);
        return products.map(productMapper::toProductResponse);
    }

    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(productMapper::toProductResponse);
    }

    public Page<ProductResponse> getAllProductsByVendor(Long vendorId, Pageable pageable) {
        if (vendorId == null) {
            throw new IllegalArgumentException("Vendor ID is required");
        }
        Page<Product> products = productRepository.findByVendorId(vendorId, pageable);
        return products.map(productMapper::toProductResponse);
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
        return productMapper.toProductResponse(product);
    }

    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return Page.empty(pageable);
        }
        return productRepository.searchAllBy(keyword, pageable).map(productMapper::toProductResponse);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Vendor vendor = authService.getCurrentVendor();
        if (vendor == null) {
            throw new AccessDeniedException("Authenticated user is not a vendor");
        }

        validateProductRequest(request);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.getCategoryId()));

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setBrand(request.getBrand());
        product.setCategory(category);
        product.setVendor(vendor);
        product.setActive(request.getStatus() != null ? request.getStatus() : ProductStatus.ACTIVE);
        product.setCreatedAt(LocalDateTime.now());

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                String fileName = saveImage(request.getImage());
                product.setImageName(fileName);
                product.setImageType(request.getImage().getContentType());
            } catch (IOException e) {
                logger.error("Failed to upload image for product: {}", request.getName(), e);
                throw new ImageProcessingException("Failed to upload image", e);
            }
        }
        Product savedProduct = productRepository.save(product);
        return productMapper.toProductResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest productRequest, MultipartFile image) {
        Vendor vendor = authService.getCurrentVendor();
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        if (!product.getVendor().getId().equals(vendor.getId())) {
            throw new AccessDeniedException("You can only update your own products");
        }

        Category category = categoryRepository.findById(productRequest.getCategoryId())
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

    public Page<ProductResponse> getAllSorted(String sortBy, Pageable pageable) {
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.unsorted();

        if (!sortBy.isBlank() && !pageable.getSort().isSorted()) {
            if (!VALID_SORT_FIELDS.contains(sortBy.toLowerCase())) {
                throw new IllegalArgumentException("Invalid sort field: " + sortBy);
            }
            String[] parts = sortBy.split("_");
            if (parts.length == 2) {
                String field = parts[0];
                Sort.Direction direction = parts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
                sort = Sort.by(direction, field);
            }
        }

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );

        Page<Product> products = productRepository.findAll(sortedPageable);
        return products.map(productMapper::toProductResponse);
    }

    public Product getProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
    }

    private void validateProductRequest(ProductRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
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
        if (image.getSize() > 5 * 1024 * 1024) { // 5MB limit
            throw new IllegalArgumentException("Image size exceeds 5MB");
        }

        String imageName = "product-" + UUID.randomUUID() + "." + extension;
        Path uploadDir = Paths.get(UPLOAD_DIR);
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
                Path imagePath = Paths.get(UPLOAD_DIR, imageName);
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


    public Page<ProductResponse> filterProducts(@NotNull Map<String, Object> filters, Pageable pageable) {

        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        Long categoryId = null;
        String brand = null;
        String name = null;


        if (filters.containsKey("priceRange")) {
            String priceRange = (String) filters.get("priceRange");
            if (priceRange != null && !priceRange.isEmpty()) {
                try {
                    String[] range = priceRange.split("-");
                    if (range.length == 2) {
                        minPrice = new BigDecimal(range[0].trim());
                        maxPrice = new BigDecimal(range[1].trim());
                    } else {
                        minPrice = maxPrice = new BigDecimal(priceRange.trim());
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid priceRange format: " + priceRange);
                }
            }
        }
        if (filters.containsKey("categoryId")) {
            categoryId = (Long) filters.get("categoryId");
        }
        if (filters.containsKey("brand")) {
            brand = (String) filters.get("brand");
        }
        if (filters.containsKey("name")) {
            name = (String) filters.get("name");
        }

//        if (filters.containsKey("categoryId")) {
//            categoryId = (Long) filters.get("categoryId");
//            if (categoryId != null && !categoryRepository.existsById(categoryId)) {
//                throw new IllegalArgumentException("Category with ID " + categoryId + " does not exist.");
//            }
//        }

        List<Specification<Product>> specs = new ArrayList<>();
        if (minPrice != null && maxPrice != null) {
            BigDecimal finalMinPrice = minPrice;
            BigDecimal finalMaxPrice = maxPrice;
            specs.add((root, query, cb) -> cb.between(root.get("price"), finalMinPrice, finalMaxPrice));
        }
        if (categoryId != null) {
            Long finalCategoryId = categoryId;
            specs.add((root, query, cb) -> cb.equal(root.get("category").get("id"), finalCategoryId));
        }
        if (brand != null && !brand.isEmpty()) {
            String finalBrand = brand;
            specs.add((root, query, cb) -> cb.like(cb.lower(root.get("brand")), "%" + finalBrand.toLowerCase() + "%"));
        }
        if (name != null && !name.isEmpty()) {
            String finalName = name;
            specs.add((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + finalName.toLowerCase() + "%"));
        }

        Specification<Product> finalSpec = specs.stream()
                .reduce((root, query, cb) -> cb.conjunction(), Specification::and);

        Page<Product> products = productRepository.findAll(finalSpec, pageable);
        return products.map(this::mapToProductResponse);
    }

    
    private ProductResponse mapToProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setCategoryId(product.getCategory().getId());
        return response;
    }
}