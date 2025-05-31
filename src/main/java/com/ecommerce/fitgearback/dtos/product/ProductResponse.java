package com.ecommerce.fitgearback.dtos.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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
    private LocalDateTime createdAt;
    private ProductStatus status;
    private String imageName;
    private String imageType;
    private String imageUrl;
    private Long quantity;
    private String imageBase64;
    private Long vendorId;
    private Long categoryId;
    private String categoryName;
}
