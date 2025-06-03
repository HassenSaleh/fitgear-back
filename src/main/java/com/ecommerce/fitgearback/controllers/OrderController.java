//package com.ecommerce.fitgearback.controllers;
//
//import com.ecommerce.fitgearback.dtos.order.OrderItemRequest;
//import com.ecommerce.fitgearback.dtos.order.OrderRequest;
//import com.ecommerce.fitgearback.dtos.order.OrderResponse;
//import com.ecommerce.fitgearback.dtos.order.PlaceOrderRequest;
//import com.ecommerce.fitgearback.enums.OrderStatus;
//import com.ecommerce.fitgearback.enums.PaymentMethod;
//import com.ecommerce.fitgearback.enums.PaymentStatus;
//import com.ecommerce.fitgearback.models.Customer;
//import com.ecommerce.fitgearback.models.Order;
//import com.ecommerce.fitgearback.models.OrderItem;
//import com.ecommerce.fitgearback.models.Product;
//import com.ecommerce.fitgearback.repositories.OrderRepository;
//import com.ecommerce.fitgearback.repositories.ProductRepository;
//import com.ecommerce.fitgearback.services.CustomerService;
//import com.ecommerce.fitgearback.services.OrderService;
//import jakarta.validation.Valid;
//import lombok.AllArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//@CrossOrigin(origins = "http://localhost:5173" , allowCredentials = "true")
//@AllArgsConstructor
//@RestController
//@RequestMapping("/api/v1/orders")
//public class OrderController {
//    private final OrderService orderService;
//    private final OrderRepository orderRepository;
//    private final ProductRepository productRepository;
//
//    @GetMapping("/")
//    public ResponseEntity<List<OrderResponse>> getAllOrders() {
//        List<OrderResponse> orders = orderService.getAllOrders();
//        return ResponseEntity.ok(orders);
//    }
//    @GetMapping("/{orderId}")
//    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
//        OrderResponse order = orderService.getOrder(orderId);
//        return ResponseEntity.ok(order);
//    }
//
//    @PostMapping("/")
//    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
//        OrderResponse orderDto = orderService.placeOrder(request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(orderDto);
//    }
//
//    @GetMapping("/customer/{customerId}")
//    public ResponseEntity<List<OrderResponse>> getOrdersByCustomerId(@PathVariable Long customerId) {
//        List<OrderResponse> orders = orderService.getOrdersByCustomerId(customerId);
//        return ResponseEntity.ok(orders);
//    }
//
//    @GetMapping("/vendor/{vendorId}")
//    public ResponseEntity<List<OrderResponse>> getOrdersByVendorId(@PathVariable Long vendorId) {
//        List<OrderResponse> orders = orderService.getOrdersByVendorId(vendorId);
//        return ResponseEntity.ok(orders);
//    }
//
//    @PostMapping
//    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequest orderRequest, Authentication authentication) {
//        Order order = new Order();
//        order.setCustomer(new Customer()); // Set from authentication
//        order.setTotalAmount(orderRequest.getTotalAmount());
//        order.setStatus(OrderStatus.PENDING);
//        order.setPaymentMethod(orderRequest.getPaymentMethod());
//        order.setPaymentStatus(PaymentStatus.PENDING);
//        order.setTxRef("tx-" + UUID.randomUUID().toString());
//        order.setShippingAddress(orderRequest.getShippingAddress());
//
//        List<OrderItem> items = new ArrayList<>();
//        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
//            Product product = productRepository.findById(itemRequest.getProductId())
//                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemRequest.getProductId()));
//            OrderItem item = new OrderItem(order, product, itemRequest.getQuantity());
//            items.add(item);
//        }
//        order.setItems(items);
//
//        orderRepository.save(order);
//        return ResponseEntity.ok(order);
//    }
//
//}



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

//    @GetMapping
//    public ResponseEntity<Page<OrderResponse>> getAllOrders(Pageable pageable) {
//        Page<OrderResponse> orders = orderService.getAllOrders(pageable);
//        return ResponseEntity.ok(orders);
//    }
//
//    @GetMapping("/{orderId}")
//    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
//        OrderResponse order = orderService.getOrder(orderId);
//        return ResponseEntity.ok(order);
//    }
//
//    @PostMapping
//    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
//        OrderResponse orderDto = orderService.placeOrder(request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(orderDto);
//    }
//
//    @PostMapping("/cart")
//    public ResponseEntity<OrderResponse> placeOrderFromCart(@Valid @RequestBody PlaceOrderRequest request) {
//        OrderResponse orderDto = orderService.placeOrderFromCart(request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(orderDto);
//    }
//
//    @GetMapping("/customer/{customerId}")
//    public ResponseEntity<Page<OrderResponse>> getOrdersByCustomerId(@PathVariable Long customerId, Pageable pageable) {
//        Page<OrderResponse> orders = orderService.getOrdersByCustomerId(customerId, pageable);
//        return ResponseEntity.ok(orders);
//    }
//
//    @GetMapping("/vendor/{vendorId}")
//    public ResponseEntity<Page<OrderResponse>> getOrdersByVendorId(@PathVariable Long vendorId, Pageable pageable) {
//        Page<OrderResponse> orders = orderService.getOrdersByVendorId(vendorId, pageable);
//        return ResponseEntity.ok(orders);
//    }



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