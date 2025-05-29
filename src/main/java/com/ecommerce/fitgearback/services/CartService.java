package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.dtos.cart.CartItemResponse;
import com.ecommerce.fitgearback.dtos.cart.CartResponse;
import com.ecommerce.fitgearback.exceptions.CartNotFoundException;
import com.ecommerce.fitgearback.exceptions.ProductNotFoundException;
import com.ecommerce.fitgearback.mappers.CartMapper;
import com.ecommerce.fitgearback.models.Cart;
import com.ecommerce.fitgearback.repositories.CartRepository;
import com.ecommerce.fitgearback.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
public class CartService {
    private final CartRepository cartRepository;
    private CartMapper cartMapper;
    private ProductRepository productRepository;

    public CartResponse createCart() {
        var cart = new Cart();
        cartRepository.save(cart);

        return cartMapper.toCartResponse(cart);
    }

    public CartItemResponse addToCart(UUID cartId, Long productId) {
        var cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) {
            throw new CartNotFoundException();
        }

        var product = productRepository.findById(productId)
                    .orElse(null);
        if (product == null) {
            throw new ProductNotFoundException();
        }

        var cartItem = cart.addItem(product);

        cartRepository.save(cart);

        return cartMapper.toCartItemResponse(cartItem);
    }

    public CartResponse getCart(UUID cartId) {
        var cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) {
            throw new CartNotFoundException();
        }
        return cartMapper.toCartResponse(cart);
    }

    public CartItemResponse updateItem(UUID cartId, Long productId, Integer quantity) {
        var cart = cartRepository.getCartWithItems(cartId)
                .orElse(null);
        if (cart == null) {
            throw new CartNotFoundException();
        }

        var cartItem = cart.getItem(productId);
        if (cartItem == null) {
            throw new ProductNotFoundException();
        }

        cartItem.setQuantity(quantity);
        cartRepository.save(cart);

        return cartMapper.toCartItemResponse(cartItem);
    }

    public void removeItem(UUID cartId, Long productId) {
        var cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) {
            throw new CartNotFoundException();
        }

        cart.removeItem(productId);

        cartRepository.save(cart);
    }

    public void clearCart(UUID cartId) {
        var cart = cartRepository.getCartWithItems(cartId)
                .orElse(null);
        if (cart == null) {
            throw new CartNotFoundException();
        }
        cart.clear();

        cartRepository.save(cart);
    }

}
