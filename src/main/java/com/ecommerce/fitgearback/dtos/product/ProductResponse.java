package com.ecommerce.fitgearback.dtos.product;

import com.ecommerce.fitgearback.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String brand;
    private LocalDateTime createdAt;
    private ProductStatus status;
    private String imageUrl;
    private Long vendorId;
    private Long categoryId;
    private Long quantity;
    private String categoryName;
}
