package com.ecommerce.fitgearback.dtos.order;

import lombok.Data;

import java.util.List;

@Data
public class PlaceOrderRequest {

    private List<Long> productIds;
    private String shippingAddress;
    private String paymentMethod;

}
