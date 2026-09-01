package com.linkedin.userservice.service;

import com.linkedin.userservice.dto.AuthResponse;
import com.linkedin.userservice.dto.LoginRequest;
import com.linkedin.userservice.dto.RegisterRequest;
import com.linkedin.userservice.entity.User;
import com.linkedin.userservice.entity.UserRole;
import com.linkedin.userservice.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final KafkaTemplate<String,Object> kafkaTemplate;

    private static final String USER_CREATED_TOPIC = "user.created";

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;
    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user: {}",request.getEmail());

        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already registered: "+request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .headline(request.getHeadline())
                .location(request.getLocation())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.NORMAL_USER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered: {}",savedUser.getId());

        // publish user.created event
        //search service consume this and indexes user

        Map<String,Object> userCreatedEvent = new HashMap<>();
        userCreatedEvent.put("userId",savedUser.getId());
        userCreatedEvent.put("firstName",savedUser.getFirstName());
        userCreatedEvent.put("lastName",savedUser.getLastName());
        userCreatedEvent.put("headline",savedUser.getHeadline());
        userCreatedEvent.put("location",savedUser.getLocation());

        kafkaTemplate.send(USER_CREATED_TOPIC,savedUser.getId(),userCreatedEvent);

        log.info("user.created event published: {}",savedUser.getId());

        String token = generateToken(savedUser.getId(),savedUser.getEmail());

        return buildAuthResponse(savedUser,token);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .accessToken(token)
                .refreshToken(
                        generateRefreshToken(user.getId())
                )
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    /**
     * Generate refresh token
     *
     * used to get new Access token when it expires
     * Server validates and return new access token
     * @param userId
     * @return
     */
    private String generateRefreshToken(String userId) {
        return Jwts.builder()
                .claim("userId",userId)
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSignInKey())
                .compact();
    }


    /**
     * Generates access token
     * @param userId
     * @param email
     * @return
     */
    private String generateToken(String userId, String email) {
        return Jwts.builder()
                .claim("userId",userId)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt: {}",request.getEmail());

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() ->
                    new RuntimeException("User Not found: "+request.getEmail())
        );

        //Bcrypt verify raw password with hash
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("Invalid Credentials");
        }

        log.info("Login successful: {}",user.getId());

        // Generate Jwt Token

        String token = generateToken(user.getId(),user.getEmail());

        return buildAuthResponse(user,token);
    }

}
