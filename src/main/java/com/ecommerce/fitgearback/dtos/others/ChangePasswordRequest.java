package com.ecommerce.fitgearback.dtos.others;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
}


