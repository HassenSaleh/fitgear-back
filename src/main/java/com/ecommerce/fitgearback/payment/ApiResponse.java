package com.ecommerce.fitgearback.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
@AllArgsConstructor
@Data
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;
}