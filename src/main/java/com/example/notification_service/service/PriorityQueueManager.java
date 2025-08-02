package com.example.notification_service.service;

import com.example.notification_service.dto.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PriorityQueueManager {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendNotification(NotificationMessage message) {
        String topic = switch (message.getPriority()) {
            case HIGH -> "notifications-high";
            case NORMAL -> "notifications-normal";
            case LOW -> "notifications-low";
        };

        kafkaTemplate.send(topic, message.getUserId(), message);
        log.info("Queued notification {} to topic {}", message.getId(), topic);
    }
}

