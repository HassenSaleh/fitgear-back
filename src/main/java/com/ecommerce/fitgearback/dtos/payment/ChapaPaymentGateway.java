package com.ecommerce.fitgearback.dtos.payment;

import com.ecommerce.fitgearback.models.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

//@Service
public class ChapaPaymentGateway implements PaymentGateway{

    @Value("${websiteUrl}")
    private String websiteUrl;

    @Value("${stripe.webhookSecretKey}")
    private String webhookSecretKey;
    @Override
    public CheckoutSession createCheckoutSession(Order order) {
        return null;
    }

    @Override
    public Optional<PaymentResult> parseWebhookRequest(WebhookRequest request) {
        return Optional.empty();
    }

//    private Long extractOrderId(Event event) {
//        var stripeObject = event.getDataObjectDeserializer().getObject().orElseThrow(
//                () -> new PaymentException("Could not deserialize Stripe event. Check the SDK and API version.")
//        );
//        var paymentIntent = (PaymentIntent) stripeObject;
//        return Long.valueOf(paymentIntent.getMetadata().get("order_id"));
//    }
}
