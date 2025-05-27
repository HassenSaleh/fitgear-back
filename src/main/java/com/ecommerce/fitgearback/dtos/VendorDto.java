package com.ecommerce.fitgearback.dtos;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VendorDto extends UserDto{
    private String companyName;
    private String companyLogo;
    private String companyAddress;
    private String contactNumber;
}
