package com.linkedin.user_service.dto;

import com.linkedin.user_service.entity.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;

    private String email;

    private String password;
    private String firstname;
    private String lastname;

    private String headline; // 一句话简介

    private String about; // 个人介绍

    private String location; // 所在地

    private String profilePhotoUrl; // 头像图片地址

    private String coverPhotoUrl; // 封面图片地址

    private UserRole userRole;

    private List<String> skills= new ArrayList<>();

    private LocalDateTime createAt;

    private LocalDateTime updateAt;
}
