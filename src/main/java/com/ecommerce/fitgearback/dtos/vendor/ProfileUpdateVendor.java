package com.ecommerce.fitgearback.dtos.vendor;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateVendor {
    private String firstName;
    private String lastName;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    @NotBlank(message = "Company name is required")
    private String companyName;
    @NotBlank(message = "Company address is required")
    private String companyAddress;
    private String contactNumber;
}
