package com.ecommerce.fitgearback.dtos;

import com.ecommerce.fitgearback.models.Admin;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class AdminDto {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String department;
}
