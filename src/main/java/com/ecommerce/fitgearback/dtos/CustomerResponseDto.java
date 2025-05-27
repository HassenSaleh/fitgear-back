package com.ecommerce.fitgearback.dtos;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class CustomerResponseDto{

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String shippingAddress;

}
