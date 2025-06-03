package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.dtos.cart.CartItemResponse;
import com.ecommerce.fitgearback.dtos.cart.CartResponse;
import com.ecommerce.fitgearback.enums.ProductStatus;
import com.ecommerce.fitgearback.exceptions.CartNotFoundException;
import com.ecommerce.fitgearback.exceptions.ProductNotFoundException;
import com.ecommerce.fitgearback.mappers.CartMapper;
import com.ecommerce.fitgearback.models.Cart;
import com.ecommerce.fitgearback.models.CartItem;
import com.ecommerce.fitgearback.models.Customer;
import com.ecommerce.fitgearback.models.Product;
import com.ecommerce.fitgearback.repositories.CartItemRepository;
import com.ecommerce.fitgearback.repositories.CartRepository;
import com.ecommerce.fitgearback.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;
    private final AuthService authService;

    @Transactional
    public CartResponse createOrGetCart() {
        Customer customer = authService.getCurrentCustomer();
        return cartRepository.findByCustomerId(customer.getId())
                .map(cartMapper::toCartResponse)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setCustomer(customer);
                    cartRepository.save(cart);
                    logger.info("Created cart for customer ID: {}", customer.getId());
                    return cartMapper.toCartResponse(cart);
                });
    }

    @Transactional
    public CartItemResponse addToCart(Long productId, Integer quantity) {
        Customer customer = authService.getCurrentCustomer();
        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new CartNotFoundException("Cart not found for customer ID: " + customer.getId()));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        if (product.getActive() != ProductStatus.ACTIVE) {
            throw new IllegalArgumentException("Product is not active: " + product.getName());
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }

        CartItem cartItem = cart.addItem(product, quantity);
        cartRepository.save(cart);
        logger.debug("Added product ID: {} to cart for customer ID: {}", productId, customer.getId());
        return cartMapper.toCartItemResponse(cartItem);
    }

    public CartResponse getCart() {
        Customer customer = authService.getCurrentCustomer();
        Cart cart = cartRepository.findByIdWithItemsFetched(customer.getId())
                .orElseThrow(() -> new CartNotFoundException("Cart not found for customer ID: " + customer.getId()));
        return cartMapper.toCartResponse(cart);
    }

    @Transactional
    public CartItemResponse updateItem(Long productId, Integer quantity) {
        Customer customer = authService.getCurrentCustomer();
        Cart cart = cartRepository.findByIdWithItemsFetched(customer.getId())
                .orElseThrow(() -> new CartNotFoundException("Cart not found for customer ID: " + customer.getId()));

        CartItem cartItem = cart.getItem(productId);
        if (cartItem == null) {
            throw new ProductNotFoundException("Product not found with ID: " + productId + " in cart");
        }

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (cartItem.getProduct().getStock() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product: " + cartItem.getProduct().getName());
        }

        cartItem.setQuantity(quantity);
        cartRepository.save(cart);
        logger.debug("Updated quantity for product ID: {} in cart for customer ID: {}", productId, customer.getId());
        return cartMapper.toCartItemResponse(cartItem);
    }

    @Transactional
    public void removeItem(Long productId) {
        Customer customer = authService.getCurrentCustomer();
        Cart cart = cartRepository.findByIdWithItemsFetched(customer.getId())
                .orElseThrow(() -> new CartNotFoundException("Cart not found for customer ID: " + customer.getId()));

        if (!cart.removeItem(productId)) {
            throw new ProductNotFoundException("Product not found with ID: " + productId + " in cart");
        }
        cartRepository.save(cart);
        logger.debug("Removed product ID: {} from cart for customer ID: {}", productId, customer.getId());
    }

    @Transactional
    public void clearCart() {
        Customer customer = authService.getCurrentCustomer();
        Cart cart = cartRepository.findByIdWithItemsFetched(customer.getId())
                .orElseThrow(() -> new CartNotFoundException("Cart not found for customer ID: " + customer.getId()));
        cart.clear();
        cartRepository.save(cart);
        logger.info("Cleared cart for customer ID: {}", customer.getId());
    }

    public List<CartItemResponse> getCartItems() {
        Customer customer = authService.getCurrentCustomer();
        List<CartItem> cartItems = cartItemRepository.findByCartCustomerId(customer.getId());
        return cartItems.stream()
                .map(cartMapper::toCartItemResponse)
                .toList();
    }

    public CartResponse getCartWithItems(Long id) {
        Cart cart = cartRepository.findByIdWithItemsFetched(id)
                .orElseThrow(() -> new CartNotFoundException("Cart not found with ID: " + id));
        return cartMapper.toCartResponse(cart);
    }
}