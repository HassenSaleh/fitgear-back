package com.ecommerce.fitgearback.mappers;

import com.ecommerce.fitgearback.dtos.cart.*;
import com.ecommerce.fitgearback.models.Cart;
import com.ecommerce.fitgearback.models.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(source = "customerId", target = "customer.id")
    Cart toCart(CartRequest cartRequest);

    @Mapping(source = "customer.id", target = "customerId")
    CartResponse toCartResponse(Cart cart);
    //@Mapping(source = "productId", target = "product.id")
    CartItem toItemCart(CartItemRequest cartItemRequest);
    CartItemResponse toCartItemResponse(CartItem cartItem);
}
