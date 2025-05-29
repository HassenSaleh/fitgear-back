package com.ecommerce.fitgearback.dtos.cart;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest{
    @NotBlank(message = "Product id should not be null")
    private Long productId;
    @NotBlank(message = "Quantity should not be null")
    private Integer quantity;
    @NotBlank(message = "Total price should not be null")
    private BigDecimal totalPrice;
}