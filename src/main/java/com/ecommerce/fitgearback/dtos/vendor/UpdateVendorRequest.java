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
public class UpdateVendorRequest {
    @NotBlank(message = "First name is required")
    private String firstName;
    @NotBlank(message = "Last name is required")
    private String lastName;
    @NotBlank(message = "Contact number is required")
    private String contactNumber;
    @NotBlank(message = "Company name is required")
    private String companyName;
    @NotBlank(message = "Company address is required")
    private String companyAddress;
}
