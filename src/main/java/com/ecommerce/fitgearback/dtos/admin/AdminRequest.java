package com.ecommerce.fitgearback.dtos.admin;

import com.ecommerce.fitgearback.dtos.user.UserRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminRequest extends UserRequest {
    private String department;

}
