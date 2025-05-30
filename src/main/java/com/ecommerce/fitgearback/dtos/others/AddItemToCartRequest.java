package com.ecommerce.fitgearback.dtos.others;

import lombok.Data;

import java.util.UUID;

@Data
public class AddItemToCartRequest {
    private Long productId;
}
