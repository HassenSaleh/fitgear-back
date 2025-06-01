package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.order.OrderResponse;
import com.ecommerce.fitgearback.dtos.others.ErrorDto;
import com.ecommerce.fitgearback.exceptions.OrderNotFoundException;
import com.ecommerce.fitgearback.services.OrderService;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173" , allowCredentials = "true")


@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/order")
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/all")
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable("orderId") Long orderId) {
        return orderService.getOrder(orderId);
    }


}
