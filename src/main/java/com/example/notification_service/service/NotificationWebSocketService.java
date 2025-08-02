package com.example.notification_service.service;

import com.example.notification_service.dto.DeliveryStatus;
import com.example.notification_service.dto.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class NotificationWebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void broadcastDeliveryStatus(NotificationMessage message, DeliveryStatus status, String channel) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("notificationId", message.getId());
        payload.put("userId", message.getUserId());
        payload.put("channel", channel);
        payload.put("status", status.name());
        payload.put("timestamp", Instant.now().toString());

        String destination = "/topic/notifications/" + message.getUserId();
        messagingTemplate.convertAndSend(destination, payload);

        log.debug("Broadcasted status {} for notification {} to {}", status, message.getId(), destination);
    }
}

