package com.ecommerce.fitgearback.dtos;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminDto extends UserDto{
    private String department;
}
