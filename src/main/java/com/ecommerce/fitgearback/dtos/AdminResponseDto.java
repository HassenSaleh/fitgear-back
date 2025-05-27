package com.ecommerce.fitgearback.dtos;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminResponseDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String department;
}
