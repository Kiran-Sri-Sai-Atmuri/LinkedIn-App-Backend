package com.linkedin.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender javaMailSender;

    /**
     * consume user.created event
     * Send welcome notification
     */
    @KafkaListener(topics = "user.created")
    public void consumeUserCreatedEvent(
            @Payload Map<String,Object> payload
    ){
        try{
            String email = (String)payload.get("email");
            String firstName = (String)payload.get("firstName");

            sendNotification(email,
                    "Welcome to LinkedIn",
                    String.format(
                            "Welcome %s your account ahs been created"+
                            "Start connecting with professionals.",
                            firstName
                    ));
        }catch (Exception e){
            log.error("Error sending welcome notification : {}",e.getMessage());
        }
    }

    @KafkaListener(topics = "connection.requested")
    public void consumeConnectionRequestedEvent(
            @Payload Map<String,Object> payload
    ){
        try{

            String receiverId = (String)payload.get("receiverId");
            String requesterId = (String)payload.get("requesterId");
            String receiverEmail = (String)payload.get("receiverEmail");
            sendNotification(receiverEmail,
                    "New connection request",
                    String.format(
                            "User %s want to connect with you",
                            requesterId
                    ));

        } catch (Exception e) {
            log.error("Error sending connection request notification : {}",e.getMessage());
        }
    }

    @KafkaListener(topics = "connection.accepted")
    public void consumeConnectionAcceptedEvent(
            @Payload Map<String,Object> payload
    ){
        try{
            String receiverId = (String)payload.get("receiverId");
            String requesterId = (String)payload.get("requesterId");
            String requesterEmail = (String)payload.get("requesterEmail");
            sendNotification(requesterEmail,
                    "Connection Accepted",
                    String.format(
                            "User %s Accepted your connection request"+
                                    "You are now connected"
                            ,
                            receiverId
                    ));
        }catch (Exception e){
            log.error("Error sending connection accepted notification: {}",e.getMessage());
        }
    }

    @KafkaListener(topics = "post.liked")
    public void consumePostLikedEvent(
            @Payload Map<String,Object> payload
    ){
        try{
            String authorId = (String)payload.get("authorId");
            String userId = (String)payload.get("userId");
            String postId = (String)payload.get("postId");

            sendNotification(authorId,
                    "Someone Liked your post",
                    String.format(
                            "User %s liked your post %s",
                            userId,postId
                    )
                    );
        } catch (Exception e) {
            log.error("Error sending like notification: {}",e.getMessage());
        }
    }

    @KafkaListener(topics = "post.commented")
    public void consumePostCommentedEvent(
            @Payload Map<String,Object> payload
    ){
        try{
            String postAuthorId = (String)payload.get("postAuthorId");
            String commenterId = (String)payload.get("authorId");
            String postId = (String)payload.get("postId");

            sendNotification(postAuthorId,
                    "New comment on your post",
                    String.format(
                            "User %s commented on your post %s",
                            commenterId,postId
                    )
            );
        } catch (Exception e) {
            log.error("Error sending comment notification: {}",e.getMessage());
        }
    }

    private void sendNotification(String email,String title, String message) {

//        log.info("---------------------------------------");
//        log.info("NOTIFICATION SENT");
//        log.info("To user: {}",email);
//        log.info("Title: {}",title);
//        log.info("message: {}",message);
//        log.info("--------------------------");

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject(title);
        mailMessage.setText(message);


    }

}
