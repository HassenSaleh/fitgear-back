package com.ecommerce.fitgearback.payment;
import com.ecommerce.fitgearback.enums.OrderStatus;
import com.ecommerce.fitgearback.enums.PaymentMethod;
import com.ecommerce.fitgearback.enums.PaymentStatus;
import com.ecommerce.fitgearback.models.Customer;
import com.ecommerce.fitgearback.models.Order;
import com.ecommerce.fitgearback.repositories.OrderRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
@RestController
@RequestMapping("/api/v1/payments")
public class ChapaController {
    private final ChapaService chapaService;
    private final OrderRepository orderRepository;
    private static final Logger logger = LoggerFactory.getLogger(ChapaService.class);

    @PostMapping("/initialize")
    public ResponseEntity<ChapaResponse> initializePayment(@Valid @RequestBody ChapaRequest request) throws IOException {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String customerEmail = authentication.getName();

            Order order;
            if (request.getOrderId() != null) {
                order = orderRepository.findByIdWithItems(request.getOrderId())
                        .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + request.getOrderId()));
                if (!order.getCustomer().getEmail().equals(customerEmail)) {
                    throw new IllegalArgumentException("Order does not belong to the authenticated customer");
                }
            } else {
                order = new Order();
                order.setCustomer(new Customer());
                order.setTotalAmount(new BigDecimal(request.getAmount()));
                order.setStatus(OrderStatus.PENDING);
                order.setPaymentMethod(PaymentMethod.CHAPA);
                order.setPaymentStatus(PaymentStatus.PENDING);
                order.setTxRef("tx-" + UUID.randomUUID());
                orderRepository.save(order);
            }
            //change it
            request.setTxRef(order.getTxRef());
            request.setEmail(customerEmail);
            request.setCallbackUrl("https://your-app.com/api/v1/payments/callback");
            request.setReturnUrl("https://your-app.com/payment-result");
            if (request.getCustomization() == null) {
                Map<String, String> customization = new HashMap<>();
                customization.put("title", "Merchant Payment");
                customization.put("description", "Order payment");
                request.setCustomization(customization);
            }

            // Initialize payment with Chapa
            ChapaResponse response = chapaService.initializePayment(request);
            logger.info("Payment initialized for order {} with txRef: {}", order.getId(), order.getTxRef());

            // Update order with payment details
            order.setPaymentStatus(PaymentStatus.PENDING);
            orderRepository.save(order);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ChapaResponse("failed", e.getMessage(), null));
        } catch (IllegalStateException e) {
            logger.error("Configuration error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChapaResponse("failed", "Configuration error: " + e.getMessage(), null));
        } catch (IOException e) {
            logger.error("Payment initialization failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChapaResponse("failed", "Payment initialization failed: " + e.getMessage(), null));
        }
    }
    @GetMapping("/verify/{txRef}")
    public ResponseEntity<Boolean> verifyPayment(@PathVariable String txRef) throws IOException {
        try {
            boolean verified = chapaService.verifyPayment(txRef);
            if (verified) {
                Order order = orderRepository.findByTxRef(txRef);
                if (order != null) {
                    order.setPaymentStatus(PaymentStatus.PAID);
                    order.setStatus(OrderStatus.CONFIRMED);
                    orderRepository.save(order);
                    logger.info("Payment verified and order {} updated to CONFIRMED", order.getId());
                }
            }
            return ResponseEntity.ok(verified);
        } catch (IOException e) {
            logger.error("Payment verification failed for txRef {}: {}", txRef, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(false);
        }
    }

    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestBody Map<String, Object> callbackData) {
        String txRef = (String) callbackData.get("txRef");
        String status = (String) callbackData.get("status");
        logger.info("Callback received for txRef: {}, status: {}", txRef, status);

        Order order = orderRepository.findByTxRef(txRef);
        if (order != null) {
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
        } else {
            logger.warn("No order found for txRef: {}", txRef);
        }

        return ResponseEntity.ok("Callback processed");
    }
}