package com.ecommerce.fitgearback.exceptions;

public class CartNotFoundException extends RuntimeException{

    public CartNotFoundException() {
        super("Cart not found");
    }
}
