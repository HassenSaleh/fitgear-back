package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.dtos.order.OrderResponse;
import com.ecommerce.fitgearback.dtos.order.PlaceOrderRequest;
import com.ecommerce.fitgearback.enums.PaymentStatus;
import com.ecommerce.fitgearback.exceptions.CartNotFoundException;
import com.ecommerce.fitgearback.exceptions.OrderNotFoundException;
import com.ecommerce.fitgearback.mappers.OrderMapper;
import com.ecommerce.fitgearback.models.*;
import com.ecommerce.fitgearback.repositories.CartRepository;
import com.ecommerce.fitgearback.repositories.OrderRepository;
import com.ecommerce.fitgearback.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final AuthService authService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        Customer customer = authService.getCurrentCustomer();
        Page<Order> orders = orderRepository.findByCustomerIdWithItems(customer.getId(), pageable);
        return orders.map(orderMapper::toOrderResponse);
    }

    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        Customer customer = authService.getCurrentCustomer();
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new AccessDeniedException("You don't have access to this order.");
        }

        return orderMapper.toOrderResponse(order);
    }

    @Transactional
    public OrderResponse placeOrder(@NotNull PlaceOrderRequest request) {
        Customer customer = authService.getCurrentCustomer();
        logger.info("Placing order for customer ID: {}", customer.getId());

        List<Map<Long, Integer>> productQuantitiesList = request.getProductQuantities();
        if (productQuantitiesList == null || productQuantitiesList.isEmpty()) {
            logger.error("No products specified in order for customer ID: {}", customer.getId());
            throw new IllegalArgumentException("No products specified in the order.");
        }

        // Flatten List<Map<Long, Integer>> into Map<Long, Integer>, summing quantities for duplicate product IDs
        Map<Long, Integer> productQuantities = productQuantitiesList.stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Integer::sum
                ));

        validateAndUpdateStock(productQuantities);

        Order order = new Order();
        order.setCustomer(customer);
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setTxRef("tx-" + java.util.UUID.randomUUID());

        List<OrderItem> orderItems = productQuantities.entrySet().stream()
                .map(entry -> {
                    Product product = productRepository.findById(entry.getKey())
                            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + entry.getKey()));
                    Integer quantity = entry.getValue();
                    if (quantity <= 0) {
                        logger.error("Invalid quantity {} for product ID: {}", quantity, entry.getKey());
                        throw new IllegalArgumentException("Invalid quantity for product ID: " + entry.getKey());
                    }
                    return new OrderItem(order, product, quantity);
                })
                .toList();
        order.setItems(orderItems);

        order.setTotalAmount(orderItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        Order savedOrder = orderRepository.save(order);
        logger.info("Order placed successfully with ID: {} for customer ID: {}", savedOrder.getId(), customer.getId());
        return orderMapper.toOrderResponse(savedOrder);
    }


    @Transactional
    public OrderResponse placeOrderFromCart(PlaceOrderRequest request) {
        Customer customer = authService.getCurrentCustomer();
        Cart cart = cartRepository.findByIdWithItemsFetched(customer.getId())
                .orElseThrow(() -> new CartNotFoundException("Cart not found for customer ID: " + customer.getId()));
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        Map<Long, Integer> productQuantities = cart.getItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getProduct().getId(),
                        CartItem::getQuantity
                ));

        validateAndUpdateStock(productQuantities);

        Order order = new Order();
        order.setCustomer(customer);
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setTxRef("tx-" + java.util.UUID.randomUUID());

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> {
                    Product product = cartItem.getProduct();
                    return new OrderItem(order, product, cartItem.getQuantity());
                })
                .toList();
        order.setItems(orderItems);

        order.setTotalAmount(orderItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        Order savedOrder = orderRepository.save(order);
        cart.clear();
        cartRepository.save(cart);

        return orderMapper.toOrderResponse(savedOrder);
    }

    public Page<OrderResponse> getOrdersByCustomerId(Long customerId, Pageable pageable) {
        Customer currentCustomer = authService.getCurrentCustomer();
        if (!currentCustomer.getId().equals(customerId)) {
            throw new AccessDeniedException("You can only view your own orders.");
        }

        Page<Order> orders = orderRepository.findByCustomerIdWithItems(customerId, pageable);
        return orders.map(orderMapper::toOrderResponse);
    }

    public Page<OrderResponse> getOrdersByVendorId(Long vendorId, Pageable pageable) {
        Vendor currentVendor = authService.getCurrentVendor();
        if (!currentVendor.getId().equals(vendorId) && !authService.getCurrentAdmin().getRoleNames().contains("ADMIN")) {
            throw new AccessDeniedException("You can only view your own orders.");
        }
        Page<Order> orders = orderRepository.findByProductsVendorId(vendorId, pageable);
        return orders.map(orderMapper::toOrderResponse);
    }

    @Transactional
    public void validateAndUpdateStock(@NotNull Map<Long, Integer> productQuantities) {
        List<Product> products = productRepository.findAllByIdWithLock(productQuantities.keySet());
        if (products.size() != productQuantities.size()) {
            throw new IllegalArgumentException("One or more products not found.");
        }
        products.forEach(product -> {
            Integer quantity = productQuantities.get(product.getId());
            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Invalid quantity for product ID: " + product.getId());
            }
            if (product.getStock() < quantity) {
                throw new IllegalArgumentException("Insufficient stock for product ID: " + product.getId());
            }
            product.setStock(product.getStock() - quantity);
        });
        productRepository.saveAll(products);
    }
}