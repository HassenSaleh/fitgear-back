package com.ecommerce.fitgearback.payment;

import lombok.Data;

@Data
public class PaymentRequest {
    private String amount;
    private String currency;
    private String email;
    private String firstName;
    private String lastName;
    private String txRef;
    private String callbackUrl;
}
