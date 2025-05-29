package com.ecommerce.fitgearback.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Date date;
    private LocalDateTime createdAt;
    private String imageName;
    private String imageType;
    private byte[] imageData;
    private Boolean active;
    private String categoryName;
    private Long quantity;

    private Long vendorId;
    private Long categoryId;
}
