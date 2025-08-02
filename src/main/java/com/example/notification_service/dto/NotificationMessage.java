package com.example.notification_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationMessage {
    private UUID id;
    private String userId;
    private String title;
    private String content;
    private Priority priority;
    private NotificationCategory category;
    private ChannelType channelType;
    private Map<String, Object> templateData;
    private Instant scheduledAt;
    private String idempotencyKey;
    private Integer attemptNumber = 0;
    private Long sequenceNumber;
}
