package com.ecommerce.fitgearback.dtos.order;

import com.ecommerce.fitgearback.enums.PaymentMethod;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class PlaceOrderRequest {

    private List<Long> productIds;
    private String shippingAddress;
    private PaymentMethod paymentMethod;
    private Map<Long, Integer> productQuantities;
}
