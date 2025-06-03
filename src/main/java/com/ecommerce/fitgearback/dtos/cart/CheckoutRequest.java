package com.ecommerce.fitgearback.dtos.cart;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckoutRequest {
    @NotBlank(message = "Cart id is required")
    private Long cartId;
}
