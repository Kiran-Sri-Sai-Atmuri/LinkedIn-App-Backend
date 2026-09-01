package com.linkedin.userservice.service;

import com.linkedin.userservice.dto.UserResponse;
import com.linkedin.userservice.entity.Connection;
import com.linkedin.userservice.entity.ConnectionStatus;
import com.linkedin.userservice.entity.User;
import com.linkedin.userservice.repository.ConnectionRepository;
import com.linkedin.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;


    private final KafkaTemplate<String,Object> kafkaTemplate;
    private static final String CONNECTION_REQUESTED_TOPIC = "connection.requested";
    private static final String CONNECTION_ACCEPTED_TOPIC = "connection.accepted";
    private static final String USER_UPDATED_TOPIC = "user.updated";

    public UserResponse getUserProfile(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: "+userId));

        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .headline(user.getHeadline())
                .about(user.getAbout())
                .location(user.getLocation())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .role(user.getRole())
                .skills(user.getSkills())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public UserResponse updateProfile(
            String userId,
            UserResponse request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: "+userId));
        user.setHeadline(request.getHeadline());
        user.setAbout(request.getAbout());
        user.setLocation(request.getLocation());
        user.setSkills(request.getSkills());

        User savedUser = userRepository.save(user);

        // Publish user.updated event
        Map<String,Object> userUpdatedEvent = new HashMap<>();
        userUpdatedEvent.put("userId",userId);
        userUpdatedEvent.put("firstName",savedUser.getFirstName());
        userUpdatedEvent.put("lastName",savedUser.getLastName());
        userUpdatedEvent.put("headline",savedUser.getHeadline());
        userUpdatedEvent.put("location",savedUser.getLocation());
        userUpdatedEvent.put("skills",savedUser.getSkills());


        kafkaTemplate.send(USER_UPDATED_TOPIC,savedUser.getId(),userUpdatedEvent);

        log.info("user.updated event published: {}",savedUser.getId());

        return mapToResponse(savedUser);
    }

    public String sendConnectionRequest(String receiverId, String requesterId) {
        if(connectionRepository.existsByRequesterIdAndReceiverId(
                requesterId,
                receiverId
        )){
            throw new RuntimeException(
              "Connection request already sent"
            );
        }

        Connection connection = Connection.builder()
                .requesterId(requesterId)
                .receiverId(receiverId)
                .status(ConnectionStatus.PENDING)
                .build();

        connectionRepository.save(connection);

        //Publish connection.requested event

        Map<String,Object> connectionRequestedEvent = new HashMap<>();
        connectionRequestedEvent.put("requesterId",requesterId);
        connectionRequestedEvent.put("receiverId",receiverId);

        kafkaTemplate.send(CONNECTION_REQUESTED_TOPIC,requesterId,connectionRequestedEvent);

        log.info("connection request sent: {} -> {}",requesterId,receiverId);
        return "Connection request sent";

    }

    public String acceptConnection(String connectionId) {
        Connection connection = connectionRepository.findById(connectionId).
                orElseThrow(() -> new RuntimeException(
                        "Connection not found: "+connectionId
                ));
        connection.setStatus(ConnectionStatus.CONNECTED);
        connectionRepository.save(connection);

        //Publish connection.accepted event
        Map<String,Object> connectionAcceptededEvent = new HashMap<>();
        connectionAcceptededEvent.put("requesterId",connection.getRequesterId());
        connectionAcceptededEvent.put("receiverId",connection.getReceiverId());

        kafkaTemplate.send(CONNECTION_ACCEPTED_TOPIC,connection.getRequesterId(),connectionAcceptededEvent);

        log.info("connection accepted: {}",connectionId);
        return "Connection accepted";
    }

    public List<UserResponse> getConnections(String userId) {

        List<Connection> connections = connectionRepository.findByRequesterIdAndStatus(userId, ConnectionStatus.CONNECTED);

        return connections.stream()
                .map(c->getUserProfile(c.getReceiverId()))
                .toList();
    }


    public UserResponse uploadProfilePhoto(String userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: "+userId));
        String photoUrl = s3Service.uploadFile(file,"profiles/"+userId+"/avatar");
        user.setProfilePhotoUrl(photoUrl);

        User savedUser = userRepository.save(user);

        log.info("Profile photo uploaded for user: {}",userId);

        return mapToResponse(savedUser);
    }

    public List<Connection> getPendingConnections(String userId) {

        return connectionRepository.findByReceiverIdAndStatus(userId,ConnectionStatus.PENDING);
    }
}
