package com.linkedin.user_service.controller;


import com.linkedin.user_service.dto.UserResponse;
import com.linkedin.user_service.service.S3Service;
import com.linkedin.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final S3Service s3Service;


    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserProfile(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String requestingUserId
    ) {
        log.info("Get Profile attempt:{},requested by:{}",
                requestingUserId,userId);

        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String requestingUserId,
            @RequestBody UserResponse request){
        log.info("Update Profile attempt: {}",requestingUserId);

        if(!userId.equals(requestingUserId)){
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
                userService.updateProfile(userId,request));
    }

    @PostMapping("/{userId}/connect")
    public ResponseEntity<String>sendConnectionRequest(
            @PathVariable String targetuserId,
            @RequestHeader("X-User-Id") String requestingUserId){
        log.info("Request Connection attempt to:{},requested by:{}",targetuserId,requestingUserId);
        return ResponseEntity.ok(
                userService.sendConnectionRequest(targetuserId,requestingUserId));
    }

    @PutMapping("/connection/{connectionId}/accept")
    public ResponseEntity<String>acceptConnection(
            @PathVariable String connectionId,
            @RequestHeader("X-User-Id") String requestingUserId){
        log.info("{} attempt accept connection from {}:",requestingUserId,connectionId);
        return ResponseEntity.ok(userService.acceptConnection(connectionId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<UserResponse>> getConnection(
            @PathVariable String userId){
        log.info("{} try to get all connections",userId);
        return ResponseEntity.ok(
                userService.getConnection(userId)
        );
    }

    @PostMapping("/{userId}/profilephoto")
    public ResponseEntity<UserResponse> uploadProfilePhoto (
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String requestingId,
            @RequestParam("file")MultipartFile file){

        if(!requestingId.equals(userId)){
            new RuntimeException("user do not login");
        }

        return ResponseEntity.ok(userService.uploadProfilePhoto(userId,file));
    }



}
