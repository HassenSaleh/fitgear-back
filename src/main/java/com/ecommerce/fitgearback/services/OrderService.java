package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.dtos.order.OrderResponse;
import com.ecommerce.fitgearback.dtos.order.PlaceOrderRequest;
import com.ecommerce.fitgearback.enums.PaymentStatus;
import com.ecommerce.fitgearback.exceptions.OrderNotFoundException;
import com.ecommerce.fitgearback.mappers.OrderMapper;
import com.ecommerce.fitgearback.models.Customer;
import com.ecommerce.fitgearback.models.Order;
import com.ecommerce.fitgearback.models.Product;
import com.ecommerce.fitgearback.repositories.CustomerRepository;
import com.ecommerce.fitgearback.repositories.OrderRepository;
import com.ecommerce.fitgearback.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Setter
@Getter
@Service
public class OrderService {

    private final AuthService authService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public List<OrderResponse> getAllOrders() {
        var customer = authService.getCurrentCustomer();
        var orders = orderRepository.getOrdersByCustomer(customer);
        if(orders.isEmpty()){
            return null;
        }
        return orders.stream()
                .map(orderMapper::toOrderResponse).toList();
    }

    public OrderResponse getOrder(Long orderId) {
        var order = orderRepository
                .getOrderWithItems(orderId)
                .orElseThrow(OrderNotFoundException::new);

        var user = authService.getCurrentCustomer();
        if (!order.isPlacedBy(user)) {
            throw new AccessDeniedException("You don't have access to this order.");
        }

        return orderMapper.toOrderResponse(order);
    }

    public OrderResponse placeOrder(Long customerId, PlaceOrderRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        List<Product> products = productRepository.findAllById(request.getProductIds());

        Order order = new Order();
        order.setCustomer(customer);
        order.setProducts(products);
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(PaymentStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        return orderMapper.toOrderResponse(savedOrder);
    }

    public List<OrderResponse> getOrdersByCustomerId(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        return orders.stream().map(orderMapper::toOrderResponse).toList();
    }

    public List<OrderResponse> getOrdersByVendorId(Long vendorId) {
        List<Order> orders = orderRepository.findByProductsVendorId(vendorId);
        return orders.stream().map(orderMapper::toOrderResponse).toList();
    }

}
