package com.ecommerce.fitgearback.exceptions;

public class CartEmptyException extends RuntimeException{
    public CartEmptyException() { super("Cart is empty"); }
}
