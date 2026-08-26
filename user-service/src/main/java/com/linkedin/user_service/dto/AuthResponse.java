package com.linkedin.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String userId;

    private String email;

    private String password;

    private String firstname;

    private String lastname;

    private String tokenType="Bearer";

    private String accessToken;

    private String refreshToken;


}
