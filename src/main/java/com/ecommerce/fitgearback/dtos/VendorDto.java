package com.ecommerce.fitgearback.dtos;

import com.ecommerce.fitgearback.models.Vendor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VendorDto {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @Email
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private String companyName;
    @NotBlank
    private String companyAddress;
    @NotBlank
    private String contactNumber;
}
