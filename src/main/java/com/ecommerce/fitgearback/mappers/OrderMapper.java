package com.ecommerce.fitgearback.mappers;

import com.ecommerce.fitgearback.dtos.order.*;
import com.ecommerce.fitgearback.models.Order;
import com.ecommerce.fitgearback.models.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "customerId", target = "customer.id")
    Order toOrder(OrderRequest orderRequest);

    @Mapping(source = "totalAmount", target = "totalAmount")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "paymentStatus", target = "paymentStatus")
    @Mapping(source = "shippingAddress", target = "shippingAddress")
    @Mapping(source = "paymentMethod", target = "paymentMethod")
    @Mapping(source = "txRef", target = "txRef")
    OrderResponse toOrderResponse(Order order);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.price", target = "price")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(source = "product.description", target = "description")
    @Mapping(source = "product.stock", target = "stock")
    @Mapping(source = "product.vendor.id", target = "vendorId")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);
}