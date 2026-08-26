package com.linkedin.user_service.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;





@Data
@AllArgsConstructor
@NoArgsConstructor

public class RegisterRequest {

    @NotBlank(message = "Email is Required")
    @Email(message = "Invalid Email Format")
    private String email;


    @NotBlank(message = "Password is Required")
    @Size(min = 6,message = "Password must be at least 6 characters")
    private String password;


    @NotBlank(message = "First name is Required")
    private String firstname;

    @NotBlank(message = "Last name is Required")
    private String lastname;

    private String headline; // 一句话简介

    private String location; // 所在地

}
