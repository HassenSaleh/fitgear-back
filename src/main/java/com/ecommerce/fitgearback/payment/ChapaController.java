package com.ecommerce.fitgearback.payment;

import com.ecommerce.fitgearback.enums.OrderStatus;
import com.ecommerce.fitgearback.enums.PaymentMethod;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    public ResponseEntity<ChapaResponse> initializePayment(@Valid @RequestBody ChapaRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String customerEmail = authentication.getName();

            // Require orderId to ensure valid order context
            if (request.getOrderId() == null) {
                throw new IllegalArgumentException("Order ID is required");
            }

            Order order = orderRepository.findByIdWithItems(request.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + request.getOrderId()));

            if (!order.getCustomer().getEmail().equals(customerEmail)) {
                throw new IllegalArgumentException("Order does not belong to the authenticated customer");
            }

            // Ensure txRef is set
            if (order.getTxRef() == null) {
                order.setTxRef("tx-" + UUID.randomUUID());
                order.setPaymentMethod(PaymentMethod.CHAPA);
                order.setPaymentStatus(PaymentStatus.PENDING);
                orderRepository.save(order);
            }

            // Set payment request details
            request.setTxRef(order.getTxRef());
            request.setEmail(customerEmail);
            request.setCallbackUrl(callbackUrl);
            request.setReturnUrl(returnUrl);
            if (request.getCustomization() == null) {
                Map<String, String> customization = new HashMap<>();
                customization.put("title", "Merchant Payment");
                customization.put("description", "Order payment for ID: " + order.getId());
                request.setCustomization(customization);
            }

            // Initialize payment
            ChapaResponse response = chapaService.initializePayment(request);
            logger.info("Payment initialized for order {} with txRef: {}", order.getId(), order.getTxRef());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            //logger.error("Validation error for customer {}: {}", customerEmail, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ChapaResponse("failed", e.getMessage(), null));
        } catch (IOException e) {
            //logger.error("Payment initialization failed for customer {}: {}", customerEmail, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChapaResponse("failed", "Payment initialization failed: " + e.getMessage(), null));
        }
    }


    @GetMapping("/verify/{txRef}")
    public ResponseEntity<Boolean> verifyPayment(@PathVariable String txRef) {
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
            return ResponseEntity.ok(verified);
        } catch (IllegalArgumentException e) {
            logger.error("Verification error for txRef {}: {}", txRef, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        } catch (IOException e) {
            logger.error("Payment verification failed for txRef {}: {}", txRef, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
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
