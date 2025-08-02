package com.example.notification_service.service;

import com.example.notification_service.dto.ChannelType;
import com.example.notification_service.dto.NotificationMessage;
import com.example.notification_service.dto.NotificationRequest;
import com.example.notification_service.dto.NotificationResponse;
import com.example.notification_service.model.DeliveryAttempt;
import com.example.notification_service.model.Notification;
import com.example.notification_service.model.UserPreference;
import com.example.notification_service.repo.DeliveryAttemptRepository;
import com.example.notification_service.repo.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class NotificationService {

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private UserPreferenceService userPreferenceService;

    @Autowired
    private PriorityQueueManager queueManager;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private DeliveryAttemptRepository deliveryAttemptRepository;

    public NotificationResponse processNotification(NotificationRequest request) {
        // Generate idempotency key if not provided
        if (request.getIdempotencyKey() == null) {
            request.setIdempotencyKey(idempotencyService.generateIdempotencyKey(request));
        }

        // Check for duplicate
        if (idempotencyService.isProcessed(request.getIdempotencyKey())) {
            return NotificationResponse.alreadyProcessed();
        }

        // Get user preferences
        UserPreference userPref = userPreferenceService.getUserPreference(request.getUserId());
        if (userPref.getMutedUntil() != null && userPref.getMutedUntil().isAfter(Instant.now())) {
            return NotificationResponse.builder()
                    .status("MUTED")
                    .message("User is muted until " + userPref.getMutedUntil())
                    .build();
        }

        // Save notification
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .content(request.getContent())
                .priority(request.getPriority())
                .category(request.getCategory())
                .scheduledAt(request.getScheduledAt() != null ? request.getScheduledAt() : Instant.now())
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        notification = notificationRepository.save(notification);

        // Queue for each enabled channel
        for (ChannelType channel : request.getChannels()) {
            if (isChannelEnabled(userPref, channel)) {
                NotificationMessage message = NotificationMessage.builder()
                        .id(notification.getId())
                        .userId(request.getUserId())
                        .title(request.getTitle())
                        .content(request.getContent())
                        .priority(request.getPriority())
                        .category(request.getCategory())
                        .channelType(channel)
                        .templateData(request.getTemplateData())
                        .scheduledAt(notification.getScheduledAt())
                        .idempotencyKey(request.getIdempotencyKey())
                        .build();

                if (notification.getScheduledAt().isAfter(Instant.now().plusMillis(5 * 60 * 1000))) {
                    scheduleNotification(message);
                } else {
                    queueManager.sendNotification(message);
                }
            }
        }

        idempotencyService.markAsProcessed(request.getIdempotencyKey(), notification.getId().toString());

        return NotificationResponse.success(notification.getId());
    }

    public List<NotificationResponse> processBulkNotifications(List<NotificationRequest> requests) {
        return requests.parallelStream()
                .map(this::processNotification)
                .collect(Collectors.toList());
    }

    public List<Notification> getUserNotifications(String userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public List<DeliveryAttempt> getDeliveryAttempts(UUID notificationId) {
        return deliveryAttemptRepository.findByNotificationIdOrderByAttemptedAtDesc(notificationId);
    }

    private boolean isChannelEnabled(UserPreference pref, ChannelType channel) {
        switch (channel) {
            case EMAIL: return Boolean.TRUE.equals(pref.getEmailEnabled());
            case PUSH: return Boolean.TRUE.equals(pref.getPushEnabled());
            case SMS: return Boolean.TRUE.equals(pref.getSmsEnabled());
            default: return false;
        }
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private void scheduleNotification(NotificationMessage message) {
        Instant now = Instant.now();
        Instant scheduledTime = message.getScheduledAt();

        long delayMillis = Duration.between(now, scheduledTime).toMillis();
        if (delayMillis <= 0) {
            log.info("Scheduled time already passed. Sending immediately: {}", message.getId());
            queueManager.sendNotification(message);
            return;
        }

        log.info("Scheduling notification {} to run in {} ms (at {})",
                message.getId(), delayMillis, scheduledTime);

        scheduler.schedule(() -> {
            try {
                queueManager.sendNotification(message);
            } catch (Exception e) {
                log.error("Scheduled notification failed: {}", message.getId(), e);
            }
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

}
