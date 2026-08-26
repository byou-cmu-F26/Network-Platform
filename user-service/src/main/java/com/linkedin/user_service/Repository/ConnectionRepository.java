package com.linkedin.user_service.Repository;

import com.linkedin.user_service.entity.Connection;
import com.linkedin.user_service.entity.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConnectionRepository extends JpaRepository<Connection,String> {

    boolean existsByRequesterIdandReceiverId(String targetUserId, String requestingUserId);


    List<Connection> findByRequesterIdAndStatus(String userId, ConnectionStatus connectionStatus);
}
