package com.example.notification_service.controller;

// ===== RetryNotificationController.java =====

import com.example.notification_service.dto.ChannelType;
import com.example.notification_service.dto.DeliveryStatus;
import com.example.notification_service.dto.NotificationMessage;
import com.example.notification_service.model.DeliveryAttempt;
import com.example.notification_service.model.Notification;
import com.example.notification_service.repo.DeliveryAttemptRepository;
import com.example.notification_service.repo.NotificationRepository;
import com.example.notification_service.service.PriorityQueueManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/retry")
@Slf4j
public class RetryNotificationController {

    @Autowired
    private DeliveryAttemptRepository deliveryAttemptRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PriorityQueueManager queueManager;

    @GetMapping("/failed")
    public ResponseEntity<List<DeliveryAttempt>> listFailedAttempts(
            @RequestParam(defaultValue = "FAILED") DeliveryStatus status,
            @RequestParam(defaultValue = "3600") long sinceSeconds) {

        Instant since = Instant.now().minusSeconds(sinceSeconds);
        List<DeliveryAttempt> failed = deliveryAttemptRepository.findByStatusAndAttemptedAtBefore(status, since);
        return ResponseEntity.ok(failed);
    }

    @PostMapping("/notification/{id}")
    public ResponseEntity<String> retryNotification(@PathVariable UUID id) {
        Optional<Notification> notificationOpt = notificationRepository.findById(id);

        if (notificationOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification not found");
        }

        Notification notification = notificationOpt.get();
        for (ChannelType channel : ChannelType.values()) {
            NotificationMessage message = NotificationMessage.builder()
                    .id(notification.getId())
                    .userId(notification.getUserId())
                    .title(notification.getTitle())
                    .content(notification.getContent())
                    .priority(notification.getPriority())
                    .category(notification.getCategory())
                    .channelType(channel)
                    .scheduledAt(Instant.now())
                    .idempotencyKey(notification.getIdempotencyKey() + ":retry")
                    .attemptNumber(0)
                    .build();

            queueManager.sendNotification(message);
        }

        return ResponseEntity.ok("Retry scheduled for notification " + id);
    }
}

