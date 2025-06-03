package com.ecommerce.fitgearback.mappers;

import com.ecommerce.fitgearback.dtos.order.*;
import com.ecommerce.fitgearback.models.Cart;
import com.ecommerce.fitgearback.models.Order;
import com.ecommerce.fitgearback.models.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "vendor.id", source = "vendorId")
    @Mapping(source = "customerId", target = "customer.id")
    Order toOrder(OrderRequest orderRequest);
    @Mapping(source = "vendor.id", target = "vendorId")
    @Mapping(source = "customer.id", target = "customerId")
    OrderResponse toOrderResponse(Order order);
    OrderItem toItemOrder(OrderItemRequest orderItemRequest);
    OrderItemResponse toOrderResponse(OrderItem orderItem);
}
