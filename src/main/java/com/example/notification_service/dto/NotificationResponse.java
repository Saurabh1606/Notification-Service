package com.example.notification_service.dto;

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
public class NotificationResponse {
    private UUID notificationId;
    private String status;
    private String message;

    public static NotificationResponse success(UUID notificationId) {
        return NotificationResponse.builder()
                .notificationId(notificationId)
                .status("SUCCESS")
                .message("Notification queued successfully")
                .build();
    }

    public static NotificationResponse alreadyProcessed() {
        return NotificationResponse.builder()
                .status("DUPLICATE")
                .message("Notification already processed")
                .build();
    }
}
