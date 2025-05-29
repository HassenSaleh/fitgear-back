package com.ecommerce.fitgearback.dtos.vendor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.ecommerce.fitgearback.dtos.user.UserResponse;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VendorResponse extends UserResponse {
    private String companyName;
    private String companyAddress;
    private String contactNumber;
}
