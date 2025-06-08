package com.ecommerce.fitgearback.dtos.order;

import com.ecommerce.fitgearback.enums.OrderStatus;
import com.ecommerce.fitgearback.enums.PaymentMethod;
import com.ecommerce.fitgearback.enums.PaymentStatus;
import com.ecommerce.fitgearback.models.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;
    private String shippingAddress;
    private PaymentMethod paymentMethod;
    private String txRef;
}
