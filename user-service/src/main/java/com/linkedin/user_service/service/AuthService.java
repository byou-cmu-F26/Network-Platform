package com.linkedin.user_service.service;

import com.linkedin.user_service.Repository.UserRepository;
import com.linkedin.user_service.dto.AuthResponse;
import com.linkedin.user_service.dto.LoginRequest;
import com.linkedin.user_service.dto.RegisterRequest;
import com.linkedin.user_service.entity.User;
import com.linkedin.user_service.entity.UserRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final KafkaTemplate<String,Object> kafkaTemplate;

    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    @Value("${jwt.refreshExpiration}")
    private long refreshExpiration;


    private static final String USER_CREATED_TOPIC="user.created";


    public AuthResponse register(@Valid RegisterRequest registerRequest) {
        log.info("Register user: {}", registerRequest.getEmail());

        if(userRepository.existsByEmail(registerRequest.getEmail())){
            throw new RuntimeException(
                    "Email already regstered"+registerRequest.getEmail()
            );
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(bCryptPasswordEncoder.encode(registerRequest.getPassword()));
        user.setFirstname(registerRequest.getFirstname());
        user.setLastname(registerRequest.getLastname());
        user.setHeadline(registerRequest.getHeadline());
        user.setLocation(registerRequest.getLocation());
        user.setUserRole(UserRole.NORMAL_USER);

        User savedUser = userRepository.save(user);
        log.info("User registered:{}",savedUser.getId());

        // publish user.create event
        // search service consume this event and index user
        Map<String,Object> userCreatedEvent = new HashMap<>();
        userCreatedEvent.put("userId",savedUser.getId());
        userCreatedEvent.put("firstname",savedUser.getFirstname());
        userCreatedEvent.put("lastname",savedUser.getLastname());
        userCreatedEvent.put("email",savedUser.getEmail());
        userCreatedEvent.put("headline",savedUser.getHeadline());
        userCreatedEvent.put("location",savedUser.getLocation());

        kafkaTemplate.send(USER_CREATED_TOPIC,savedUser.getId(),userCreatedEvent);
        log.info("user.created event created",savedUser.getId());

        return maptoAuthResponse(savedUser);
    }


    public AuthResponse login(@Valid LoginRequest loginRequest) {
        log.info("Login attempt:{}",loginRequest.getEmail());

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(()->new RuntimeException("User not found"+ loginRequest.getEmail()));
        if(!bCryptPasswordEncoder.matches(
                loginRequest.getPassword(),user.getPassword()
        )){throw new RuntimeException("Invalid Credentials");}

        log.info("Login successful:{}",user.getId());

        return maptoAuthResponse(user);
    }




    private AuthResponse maptoAuthResponse(User saveuser) {
        AuthResponse authResponse= new AuthResponse();
        authResponse.setEmail(saveuser.getEmail());
        authResponse.setFirstname(saveuser.getFirstname());
        authResponse.setLastname(saveuser.getLastname());
        authResponse.setUserId(saveuser.getId());
        authResponse.setPassword(saveuser.getPassword());
        authResponse.setAccessToken(generateToken(saveuser.getId(),saveuser.getEmail()));
        authResponse.setRefreshToken(generateRefreshToken(saveuser.getId()));
//        authResponse.setTokenType();
        return authResponse;
    }


    private String generateRefreshToken(String id) {
        return Jwts.builder()
                .claim("userId",id)
                .setSubject(id)
                .setIssuedAt(new Date())
                .setExpiration(new Date(
                System.currentTimeMillis()+refreshExpiration
                ))
                .signWith(getSignKey(),SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateToken(String id, String email) {
        return Jwts.builder()
                .claim("userId",id)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(
                 System.currentTimeMillis()+jwtExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignKey(){
        byte [] bytes= Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(bytes);
    }


}
