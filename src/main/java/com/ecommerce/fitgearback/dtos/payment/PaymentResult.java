package com.ecommerce.fitgearback.dtos.payment;

import com.ecommerce.fitgearback.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PaymentResult {
        private Long orderId;
        private PaymentStatus paymentStatus;
}
