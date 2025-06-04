package com.ecommerce.fitgearback.payment;

import com.ecommerce.fitgearback.enums.OrderStatus;
import com.ecommerce.fitgearback.enums.PaymentStatus;
import com.ecommerce.fitgearback.models.Order;
import com.ecommerce.fitgearback.repositories.OrderRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.Map;

@PreAuthorize("hasRole('CUSTOMER')")
@RestController
@RequestMapping("/api/v1/payments")
public class ChapaController {
    private final ChapaService chapaService;
    private final OrderRepository orderRepository;
    private static final Logger logger = LoggerFactory.getLogger(ChapaController.class);
    @Value("${app.callback-url}")
    private String callbackUrl;
    @Value("${app.return-url}")
    private String returnUrl;

    public ChapaController(ChapaService chapaService, OrderRepository orderRepository) {
        this.chapaService = chapaService;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/initialize")
    public ResponseEntity<ApiResponse<ChapaResponse>> initializePayment(@Valid @RequestBody ChapaRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String customerEmail;
            if (authentication.getPrincipal() instanceof JwtUserPrincipal) {
                customerEmail = ((JwtUserPrincipal) authentication.getPrincipal()).getEmail();
            } else {
                customerEmail = authentication.getName();
            }
            logger.debug("Authenticated customer email: {}", customerEmail);

            logger.debug("Resolved customer email: {}", customerEmail);

            if (request.getOrderId() == null) {
                logger.error("Order ID is required");
                throw new IllegalArgumentException("Order ID is required");
            }

            Long orderId;
            try {
                orderId = request.getOrderId();
            } catch (NumberFormatException e) {
                logger.error("Invalid order ID format: {}", request.getOrderId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>("error", "Invalid order ID format", null));
            }

            Order order = orderRepository.findByIdWithItems(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

            if (order.getCustomer() == null || order.getCustomer().getEmail() == null) {
                logger.error("Order {} has no associated customer", orderId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>("error", "Order has no associated customer", null));
            }

            if (!order.getCustomer().getEmail().equalsIgnoreCase(customerEmail)) {
                logger.error("Order does not belong to the authenticated customer. Order email: {}, Authenticated email: {}",
                        order.getCustomer().getEmail(), customerEmail);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>("error", "Order does not belong to the authenticated customer", null));
            }

            request.setEmail(customerEmail);
            request.setCallbackUrl(request.getCallbackUrl());
            request.setReturnUrl(request.getReturnUrl());

            ChapaResponse response = chapaService.initializePayment(order, request);
            logger.info("Payment initialized for order ID: {}", order.getId());
            return ResponseEntity.ok(new ApiResponse<>("success", "Payment initialized", response));
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(e.getMessage().startsWith("Order not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("error", e.getMessage(), null));
        } catch (IOException e) {
            logger.error("Payment initialization failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("error", "Payment initialization failed: " + e.getMessage(), null));
        }
    }


    @GetMapping("/verify/{txRef}")
    public ResponseEntity<ApiResponse<Boolean>> verifyPayment(@PathVariable String txRef) {
        try {
            if (txRef == null || !txRef.startsWith("tx-")) {
                throw new IllegalArgumentException("Invalid transaction reference");
            }

            boolean verified = chapaService.verifyPayment(txRef);
            if (verified) {
                Order order = orderRepository.findByTxRef(txRef)
                        .orElseThrow(() -> new IllegalArgumentException("No order found for txRef: " + txRef));
                order.setPaymentStatus(PaymentStatus.PAID);
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
                logger.info("Payment verified for order {} (txRef: {})", order.getId(), txRef);
            }
            return ResponseEntity.ok(new ApiResponse<>("success", "Payment verified", verified));
        } catch (IllegalArgumentException e) {
            logger.error("Verification error for txRef {}: {}", txRef, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new  ApiResponse<>("failed", e.getMessage(), null));
        } catch (IOException e) {
            logger.error("Payment verification failed for txRef {}: {}", txRef, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("error", e.getMessage(), false));
        }
    }

    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestBody Map<String, Object> callbackData) {
        String txRef = (String) callbackData.get("txRef");
        String status = (String) callbackData.get("status");
        logger.info("Callback received for txRef: {}, status: {}", txRef, status);

        if (txRef == null || status == null) {
            logger.warn("Invalid callback data: txRef or status missing");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid callback data");
        }


        Order order = orderRepository.findByTxRef(txRef)
                .orElse(null);
        if (order == null) {
            logger.warn("No order found for txRef: {}", txRef);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        }

        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            logger.info("Callback already processed for txRef: {}", txRef);
            return ResponseEntity.ok("Callback already processed");
        }

        if ("success".equalsIgnoreCase(status)) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setStatus(OrderStatus.CONFIRMED);
            logger.info("Order {} updated to PAID and CONFIRMED", order.getId());
        } else {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.FAILED);
            logger.warn("Order {} updated to FAILED due to payment status: {}", order.getId(), status);
        }
        orderRepository.save(order);

        return ResponseEntity.ok("Callback processed");
    }

}
