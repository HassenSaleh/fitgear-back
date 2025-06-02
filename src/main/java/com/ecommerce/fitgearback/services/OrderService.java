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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class OrderService {

    private final AuthService authService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    public List<OrderResponse> getAllOrders() {
        Customer customer = authService.getCurrentCustomer();
        List<Order> orders = orderRepository.findByCustomerIdWithItems(customer.getId());
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
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

        List<Map<Long, Integer>> productQuantitiesList = request.getProductQuantities();
        if (productQuantitiesList == null || productQuantitiesList.isEmpty()) {
            throw new IllegalArgumentException("No products specified in the order.");
        }

        Map<Long, Integer> productQuantities = productQuantitiesList.stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Integer::sum,
                        HashMap::new
                ));

        if (productQuantities.isEmpty()) {
            throw new IllegalArgumentException("No valid products specified in the order.");
        }

        List<Long> providedProductIds = request.getProductIds();
        if (providedProductIds != null && !providedProductIds.isEmpty()) {
            Set<Long> quantityProductIds = productQuantities.keySet();
            if (!quantityProductIds.containsAll(providedProductIds)) {
                throw new IllegalArgumentException("Provided product IDs do not match quantities.");
            }
        }

        Set<Long> productIds = productQuantities.keySet();
        List<Product> products = productRepository.findAllById(productIds);
        if (products.size() != productIds.size()) {
            throw new IllegalArgumentException("One or more products not found.");
        }
        products.forEach(product -> {
            Integer quantity = productQuantities.get(product.getId());
            if (product.getStock() < quantity) {
                throw new IllegalArgumentException("Insufficient stock for product ID: " + product.getId());
            }
        });

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


        Order order = new Order();
        order.setCustomer(customer);
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem> orderItems = products.stream()
                .map(product -> {
                    Integer quantity = productQuantities.get(product.getId());
                    if (quantity == null || quantity <= 0) {
                        throw new IllegalArgumentException("Invalid quantity for product ID: " + product.getId());
                    }
                    return new OrderItem(order, product, quantity);
                })
                .toList();
        order.setItems(orderItems);

        order.setTotalAmount(orderItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        productRepository.saveAll(products);
        Order savedOrder = orderRepository.save(order);
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

        Order order = new Order();
        order.setCustomer(customer);
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> {
                    Product product = cartItem.getProduct();
                    if (product.getStock() < cartItem.getQuantity()) {
                        throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
                    }
                    product.setStock(product.getStock() - cartItem.getQuantity());
                    return new OrderItem(order, product, cartItem.getQuantity());
                })
                .toList();
        order.setItems(orderItems);

        order.setTotalAmount(orderItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        productRepository.saveAll(orderItems.stream().map(OrderItem::getProduct).toList());
        orderRepository.save(order);
        cart.clear();
        cartRepository.save(cart);

        return orderMapper.toOrderResponse(order);
    }

    public List<OrderResponse> getOrdersByCustomerId(Long customerId) {
        Customer currentCustomer = authService.getCurrentCustomer();
        if (!currentCustomer.getId().equals(customerId)) {
            throw new AccessDeniedException("You can only view your own orders.");
        }

        List<Order> orders = orderRepository.findByCustomerIdWithItems(customerId);
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    public List<OrderResponse> getOrdersByVendorId(Long vendorId) {

        //maybe
        Vendor currentVendor = authService.getCurrentVendor();
        if (!currentVendor.getId().equals(vendorId) && !authService.getCurrentAdmin().getRoleNames().contains("ADMIN")) {
            throw new AccessDeniedException("You can only view your own orders.");
        }
        List<Order> orders = orderRepository.findByProductsVendorId(vendorId);
        return orders.stream().map(orderMapper::toOrderResponse).toList();
    }
}