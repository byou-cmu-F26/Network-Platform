package com.linkedin.user_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.ConnectionState;

import java.time.LocalDateTime;

@Entity
@Table(name="connections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Connection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String requesterId;

    @Column(nullable = false)
    private String receiverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConnectionStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updateAt;

}
