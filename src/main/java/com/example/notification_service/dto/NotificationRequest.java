package com.example.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    private String userId;

    private String title;

    private String content;

    private Priority priority = Priority.NORMAL;
    private NotificationCategory category = NotificationCategory.SYSTEM;
    private List<ChannelType> channels = Arrays.asList(ChannelType.EMAIL);
    private Map<String, Object> templateData = new HashMap<>();
    private Instant scheduledAt;
    private String idempotencyKey;
}
