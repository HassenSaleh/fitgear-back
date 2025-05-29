package com.ecommerce.fitgearback.dtos.customer;
import com.ecommerce.fitgearback.dtos.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse extends UserResponse {
    private String phoneNumber;
    private String shippingAddress;
}
