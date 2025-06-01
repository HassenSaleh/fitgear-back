package com.ecommerce.fitgearback.dtos.product;

import com.ecommerce.fitgearback.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String brand;
    private Long vendorId;
    private Long categoryId;
    private ProductStatus status;
    private Long quantity;
    private MultipartFile image;
}
