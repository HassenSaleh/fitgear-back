package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.order.OrderResponse;
import com.ecommerce.fitgearback.dtos.order.PlaceOrderRequest;
import com.ecommerce.fitgearback.services.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173" , allowCredentials = "true")
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        OrderResponse order = orderService.getOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        OrderResponse orderDto = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderDto);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomerId(@PathVariable Long customerId) {
        List<OrderResponse> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByVendorId(@PathVariable Long vendorId) {
        List<OrderResponse> orders = orderService.getOrdersByVendorId(vendorId);
        return ResponseEntity.ok(orders);
    }

}
