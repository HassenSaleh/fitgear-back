package com.ecommerce.fitgearback.mappers;

import com.ecommerce.fitgearback.dtos.order.*;
import com.ecommerce.fitgearback.models.Cart;
import com.ecommerce.fitgearback.models.Order;
import com.ecommerce.fitgearback.models.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "customerId", target = "customer.id")
    Order toOrder(OrderRequest orderRequest);
    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "createdAt", target = "orderDate")
    @Mapping(source = "totalAmount", target = "totalPrice")
    OrderResponse toOrderResponse(Order order);
    OrderItem toItemOrder(OrderItemRequest orderItemRequest);
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.vendor.id", target = "vendorId")
    OrderItemResponse toOrderResponse(OrderItem orderItem);
}
