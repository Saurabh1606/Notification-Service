package com.example.notification_service.model;

import com.example.notification_service.dto.ChannelType;
import com.example.notification_service.dto.DeliveryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "delivery_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "notification_id")
    private UUID notificationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type")
    private ChannelType channelType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DeliveryStatus status;

    @Column(name = "provider")
    private String provider;

    @Column(name = "attempt_number")
    private Integer attemptNumber;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "attempted_at")
    private Instant attemptedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;
}
