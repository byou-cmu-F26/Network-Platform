package com.linkedin.user_service.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String firstname;
    @Column(nullable = false)
    private String lastname;

    private String headline; // 一句话简介

    private String about; // 个人介绍

    private String location; // 所在地

    private String profilePhotoUrl; // 头像图片地址

    private String coverPhotoUrl; // 封面图片地址


    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @ElementCollection
    @CollectionTable(
            name = "user_skills",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "skill")
    private List<String> skills= new ArrayList<>();

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

}
