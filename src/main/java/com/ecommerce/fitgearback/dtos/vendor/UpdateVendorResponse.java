package com.ecommerce.fitgearback.dtos.vendor;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVendorResponse {
    private String firstName;
    private String lastName;
    private String email;
    private String contactNumber;
    private String companyName;
    private String companyAddress;
}
