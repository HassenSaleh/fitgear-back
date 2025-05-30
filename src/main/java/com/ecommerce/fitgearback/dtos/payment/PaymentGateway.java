package com.ecommerce.fitgearback.dtos.payment;

import com.ecommerce.fitgearback.models.Order;

import java.util.Optional;

public interface PaymentGateway {
    CheckoutSession createCheckoutSession(Order order);
    Optional<PaymentResult> parseWebhookRequest(WebhookRequest request);

}
