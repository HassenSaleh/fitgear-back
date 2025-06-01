package com.ecommerce.fitgearback.dtos.cart;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartRequest {
    @NotBlank(message = "Customer id is required")
    private Long customerId;
    private List<CartItemRequest> items = new ArrayList<>();
    private BigDecimal totalPrice = BigDecimal.ZERO;
}
