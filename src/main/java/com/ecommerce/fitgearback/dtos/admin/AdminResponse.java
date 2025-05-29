package com.ecommerce.fitgearback.dtos.admin;

import com.ecommerce.fitgearback.dtos.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminResponse extends UserResponse {
    private String department;

}
