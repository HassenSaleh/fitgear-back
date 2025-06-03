package com.ecommerce.fitgearback.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChapaResponse {
    private String status;
    private String message;
    private Map<String, String> data;
}
