package com.ecommerce.fitgearback.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Setter
@Getter
@AllArgsConstructor
public class ProductDto {
    @NotBlank
    private String name;
    @NotBlank
    private BigDecimal price;
    private String description;
    @NotBlank
    private Byte categoryId;
}
