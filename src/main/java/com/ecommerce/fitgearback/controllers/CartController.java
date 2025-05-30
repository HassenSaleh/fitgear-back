package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.cart.CartItemResponse;
import com.ecommerce.fitgearback.dtos.cart.CartResponse;
import com.ecommerce.fitgearback.dtos.cart.UpdateCartItemRequest;
import com.ecommerce.fitgearback.dtos.others.AddItemToCartRequest;
import com.ecommerce.fitgearback.exceptions.CartNotFoundException;
import com.ecommerce.fitgearback.exceptions.ProductNotFoundException;
import com.ecommerce.fitgearback.services.CartService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/cart")
@CrossOrigin(origins = "http://localhost:5173" , allowCredentials = "true")
public class CartController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartResponse> createCart(
            UriComponentsBuilder uriBuilder
    ) {
        var cartDto = cartService.createCart();
        var uri = uriBuilder
                .path("/carts/{id}")
                .buildAndExpand(cartDto.getId()).toUri();
        return ResponseEntity.created(uri).body(cartDto);
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartItemResponse> addToCart(
            @PathVariable Long cartId,
            @RequestBody AddItemToCartRequest request) {
        var cartItemDto = cartService.addToCart(cartId, request.getProductId());

        return ResponseEntity.status(HttpStatus.CREATED).body(cartItemDto);
    }

    @GetMapping("/{cartId}")
    public CartResponse getCart(@PathVariable Long cartId) {
        return cartService.getCart(cartId);
    }


    @PutMapping("/{cartId}/items/{productId}")
    public CartItemResponse updateItem(
            @PathVariable("cartId") Long cartId,
            @PathVariable("productId") Long productId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return cartService.updateItem(cartId, productId, request.getQuantity());
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<?> removeItem(
            @PathVariable("cartId") Long cartId,
            @PathVariable("productId") Long productId
    ) {
        cartService.removeItem(cartId, productId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<Void> clearCart(@PathVariable Long cartId) {
        cartService.clearCart(cartId);

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCartNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Cart not found."));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProductNotFound() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Product not found."));
    }

    @GetMapping("/customer/{customerId}")
    public CartResponse getCartByCustomer(@PathVariable Long customerId) {
        return cartService.getCartByCustomer(customerId);
    }

    @GetMapping("/customer/{customerId}/items")
    public ResponseEntity<List<CartItemResponse>> getCartItemsByCustomerId(
            @PathVariable Long customerId) {
        var items = cartService.getCartItemsByCustomerId(customerId);
        return ResponseEntity.ok(items);
    }


}
