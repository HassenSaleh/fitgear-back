package com.ecommerce.fitgearback.dtos;

import com.ecommerce.fitgearback.models.Vendor;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VendorDto {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String companyName;
    private String companyAddress;
    private String contactNumber;
}
