package com.ecommerce.fitgearback.dtos.order;

import com.ecommerce.fitgearback.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private Long customerId;
    private List<OrderItemRequest> items;
    private String shippingAddress;
    private PaymentMethod paymentMethod;
    private BigDecimal totalAmount;
}
