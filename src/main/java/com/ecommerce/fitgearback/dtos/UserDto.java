package com.ecommerce.fitgearback.dtos;

import com.ecommerce.fitgearback.annotations.Lowercase;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class UserDto {
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "First name must be less than 255 characters")
    private String firstName;
    @Size(max = 255, message = "Last name must be less than 255 characters")
    private String lastName;
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Lowercase(message = "Email must be in lowercase")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 25, message = "Password must be between 6 to 25 characters long.")
    private String password;

    @NotBlank(message = "Role is required")
    private String role;
}