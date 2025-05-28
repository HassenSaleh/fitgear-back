package com.ecommerce.fitgearback.dtos;

import lombok.*;
import org.mapstruct.Mapper;

import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Mapper(componentModel = "spring")
public class VendorResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String companyName;
    private String companyAddress;
    private String contactNumber;
    private Set<String> roles;

}
