package com.linkedin.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Email is Required")
    @Email(message = "Invalid Email Format")
    private String email;


    @NotBlank(message = "Password is Required")
    @Size(min = 6,message = "Password must be at least 6 characters")
    private String password;
}
