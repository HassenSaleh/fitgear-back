package com.ecommerce.fitgearback.mappers;

import com.ecommerce.fitgearback.dtos.CartDto;
import com.ecommerce.fitgearback.dtos.CartItemDto;
import com.ecommerce.fitgearback.models.Cart;
import com.ecommerce.fitgearback.models.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(target = "items", source = "items")
    @Mapping(target = "totalPrice", expression = "java(cart.getTotalPrice())")
    CartDto toDto(Cart cart);

    @Mapping(target = "totalPrice", expression = "java(cartItem.getTotalPrice())")
    CartItemDto toEntity(CartItem cartItem);
}
