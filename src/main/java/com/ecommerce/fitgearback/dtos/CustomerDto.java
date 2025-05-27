package com.ecommerce.fitgearback.dtos;

import com.ecommerce.fitgearback.models.User;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CustomerDto extends User {
    private String shippingAddress;
    private String phoneNumber;
}
