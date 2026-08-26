package com.linkedin.user_service.service;

import com.linkedin.user_service.Repository.ConnectionRepository;
import com.linkedin.user_service.Repository.UserRepository;
import com.linkedin.user_service.dto.UserResponse;
import com.linkedin.user_service.entity.Connection;
import com.linkedin.user_service.entity.ConnectionStatus;
import com.linkedin.user_service.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ConnectionRepository connectionRepository;
    private final KafkaTemplate<String,Object> kafkaTemplate;

    private final String CONNECTION_REQUESTED_TOPIC="connection.requested";
    private final String CONNECTION_ACCEPTED_TOPIC="connection.acccepted";
    private final String USER_UPDATED_TOPIC="user.updated";
    private final S3Service s3Service;


    public String sendConnectionRequest(String targetuserId, String requestingUserId) {

        if (!connectionRepository.existsByRequesterIdandReceiverId(targetuserId,requestingUserId)){
            throw new RuntimeException("connection exists");
        };
        Connection connection = new Connection();
        connection.setRequesterId(requestingUserId);
        connection.setReceiverId(targetuserId);
        connection.setStatus(ConnectionStatus.PENDING);
        connectionRepository.save(connection);

        Map<String,Object> connectionsendedevent=new HashMap<>();
        connectionsendedevent.put("requesterId",requestingUserId);
        connectionsendedevent.put("receiverId",targetuserId);
        kafkaTemplate.send(CONNECTION_REQUESTED_TOPIC,requestingUserId,connectionsendedevent);
        return "send success";
    }


    public String acceptConnection(String connectionId) {

        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(()->new RuntimeException("connection doesn't exist"));
        connection.setStatus(ConnectionStatus.CONNECTED);
        connectionRepository.save(connection);

        Map<String,Object> connectionacceptedevent=new HashMap<>();
        connectionacceptedevent.put("requesterId",connection.getRequesterId());
        connectionacceptedevent.put("receiverId",connection.getReceiverId());
        kafkaTemplate.send(CONNECTION_ACCEPTED_TOPIC,connection.getRequesterId(),connectionacceptedevent);
        return "connection accepted";
    }


    public List<UserResponse> getConnection(String userId) {
        List<Connection> connections = connectionRepository.findByRequesterIdAndStatus(userId,ConnectionStatus.CONNECTED);
        return connections.stream()
                .map(c->getUserProfile(c.getReceiverId()))
                .collect(Collectors.toList());
    }


    public UserResponse getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("user not found"));
        return mapToResponse(user);
    }


    // 数据库更新user的信息，之后publish event user.created
    public UserResponse updateProfile(String userId,UserResponse request) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("user not found"));
        user.setHeadline(request.getHeadline());
        user.setLocation(request.getLocation());
        user.setAbout(request.getAbout());
        user.setSkills(request.getSkills());
        User savedUser = userRepository.save(user);


        Map<String,Object> userCreatedEvent = new HashMap<>();
        userCreatedEvent.put("userId",savedUser.getId());
        userCreatedEvent.put("firstname",savedUser.getFirstname());
        userCreatedEvent.put("lastname",savedUser.getLastname());
        userCreatedEvent.put("headline",savedUser.getHeadline());
        userCreatedEvent.put("location",savedUser.getLocation());
        userCreatedEvent.put("skills",savedUser.getSkills());

        kafkaTemplate.send(USER_UPDATED_TOPIC,savedUser.getId(),userCreatedEvent);
        log.info("user.created event created",savedUser.getId());
        return mapToResponse(savedUser);

    }


    private UserResponse mapToResponse(User user) {

        UserResponse userResponse=new UserResponse();
        userResponse.setSkills(user.getSkills());
        userResponse.setLocation(user.getLocation());
        userResponse.setFirstname(user.getFirstname());
        userResponse.setLastname(user.getLastname());
        userResponse.setId(user.getId());
        userResponse.setHeadline(user.getHeadline());
        userResponse.setCreateAt(user.getCreateAt());
        userResponse.setCoverPhotoUrl(user.getCoverPhotoUrl());
        userResponse.setAbout(user.getAbout());
        userResponse.setUserRole(user.getUserRole());
        userResponse.setEmail(user.getEmail());

        return userResponse;
    }


    public UserResponse uploadProfilePhoto(String userId, MultipartFile file) {
        User user = userRepository.findById(userId).orElseThrow(()->new RuntimeException("user not login"));
        String photoUrl = s3Service.upload(file,"profiles/" +userId +"/avatar");
        user.setProfilePhotoUrl(photoUrl);
        return mapToResponse(user);
    }
}
