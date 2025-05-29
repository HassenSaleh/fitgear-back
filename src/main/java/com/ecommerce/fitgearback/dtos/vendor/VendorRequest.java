package com.ecommerce.fitgearback.dtos.vendor;

import com.ecommerce.fitgearback.dtos.user.UserRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VendorRequest extends UserRequest {
    @NotBlank(message = "Company name is required")
    private String companyName;
    @NotBlank(message = "Company address is required")
    private String companyAddress;
    private String contactNumber;
}