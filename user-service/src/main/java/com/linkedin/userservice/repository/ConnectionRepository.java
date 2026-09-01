package com.linkedin.userservice.repository;

import com.linkedin.userservice.entity.Connection;
import com.linkedin.userservice.entity.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection,String> {
    boolean existsByRequesterIdAndReceiverId(String requesterUserId, String receiverId);

    List<Connection> findByRequesterIdAndStatus(String requesterId, ConnectionStatus status);

    List<Connection> findByReceiverIdAndStatus(String receiverId, ConnectionStatus status);
}
