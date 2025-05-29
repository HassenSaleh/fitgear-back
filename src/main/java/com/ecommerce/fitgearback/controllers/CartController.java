package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.services.CartService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/cart")

public class CartController {
    private final CartService cartService;

}
