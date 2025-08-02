package com.example.notification_service.service;

import com.example.notification_service.dto.PushNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PushNotificationAggregator {

    public void sendPushNotification(String userId, PushNotification pushNotification) {
        // Simulated push aggregation/sending
        log.info("Sending push to user {}: {}", userId, pushNotification.getTitle());
        // In real implementation, route to FCM/APNS etc.
    }
}

