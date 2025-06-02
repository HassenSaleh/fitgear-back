package com.ecommerce.fitgearback.dtos.others;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ErrorDto {
    private String error;
    private int status;
}
