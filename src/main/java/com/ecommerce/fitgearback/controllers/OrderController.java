package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.order.OrderResponse;
import com.ecommerce.fitgearback.dtos.order.PlaceOrderRequest;
import com.ecommerce.fitgearback.services.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAllOrders(Pageable pageable) {
        Page<OrderResponse> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        OrderResponse order = orderService.getOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        OrderResponse orderDto = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderDto);
    }

    @PostMapping("/cart")
    public ResponseEntity<OrderResponse> placeOrderFromCart(@Valid @RequestBody PlaceOrderRequest request) {
        OrderResponse orderDto = orderService.placeOrderFromCart(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderDto);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<OrderResponse>> getOrdersByCustomerId(@PathVariable Long customerId, Pageable pageable) {
        Page<OrderResponse> orders = orderService.getOrdersByCustomerId(customerId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<Page<OrderResponse>> getOrdersByVendorId(@PathVariable Long vendorId, Pageable pageable) {
        Page<OrderResponse> orders = orderService.getOrdersByVendorId(vendorId, pageable);
        return ResponseEntity.ok(orders);
    }
}