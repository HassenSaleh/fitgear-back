package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.cart.AddItemToCartRequest;
import com.ecommerce.fitgearback.dtos.cart.CartItemResponse;
import com.ecommerce.fitgearback.dtos.cart.CartResponse;
import com.ecommerce.fitgearback.dtos.cart.UpdateCartItemRequest;
import com.ecommerce.fitgearback.dtos.order.OrderResponse;
import com.ecommerce.fitgearback.dtos.order.PlaceOrderRequest;
import com.ecommerce.fitgearback.models.Cart;
import com.ecommerce.fitgearback.services.CartService;
import com.ecommerce.fitgearback.services.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/carts")

public class CartController {

    private final CartService cartService;
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<CartResponse> createOrGetCart(UriComponentsBuilder uriBuilder) {
        CartResponse cartDto = cartService.createOrGetCart();
        var uri = uriBuilder.path("/api/v1/carts").build().toUri();
        return ResponseEntity.created(uri).body(cartDto);
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemResponse> addToCart(@Valid @RequestBody AddItemToCartRequest request) {
        CartItemResponse cartItemDto = cartService.addToCart(request.getProductId(), request.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(cartItemDto);
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        CartResponse cartDto = cartService.getCart();
        return ResponseEntity.ok(cartDto);
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartItemResponse> updateItem(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        CartItemResponse cartItemDto = cartService.updateItem(productId, request.getQuantity());
        return ResponseEntity.ok(cartItemDto);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long productId) {
        cartService.removeItem(productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/items")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/items")
    public ResponseEntity<List<CartItemResponse>> getCartItems() {
        List<CartItemResponse> items = cartService.getCartItems();
        return ResponseEntity.ok(items);
    }

    @PostMapping("/orders/from-cart")
    public ResponseEntity<OrderResponse> placeOrderFromCart(@Valid @RequestBody PlaceOrderRequest request) {
        OrderResponse orderDto = orderService.placeOrderFromCart(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderDto);
    }

}