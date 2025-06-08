package com.ecommerce.fitgearback.dtos.order;

import com.ecommerce.fitgearback.enums.PaymentMethod;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class PlaceOrderRequest {
    private String shippingAddress;
    private PaymentMethod paymentMethod;
    private List<Map<Long, Integer>> productQuantities;
}
