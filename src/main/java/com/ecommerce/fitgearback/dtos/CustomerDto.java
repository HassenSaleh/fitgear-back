package com.ecommerce.fitgearback.dtos;

import com.ecommerce.fitgearback.models.Customer;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto  {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String shippingAddress;
}
