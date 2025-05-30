package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.dtos.order.OrderResponse;
import com.ecommerce.fitgearback.exceptions.OrderNotFoundException;
import com.ecommerce.fitgearback.mappers.OrderMapper;
import com.ecommerce.fitgearback.repositories.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Setter
@Getter
@Service
public class OrderService {

    private final AuthService authService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public List<OrderResponse> getAllOrders() {
        var user = authService.getCurrentCustomer();
        var orders = orderRepository.getOrdersByCustomer(user);
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
}
