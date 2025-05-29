package com.ecommerce.fitgearback.dtos.customer;

import com.ecommerce.fitgearback.dtos.user.UserRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest extends UserRequest {
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;
}
