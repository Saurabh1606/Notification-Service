package com.example.notification_service.service;

import com.example.notification_service.dto.ChannelType;
import com.example.notification_service.dto.DeliveryStatus;
import com.example.notification_service.dto.NotificationMessage;
import com.example.notification_service.dto.PushNotification;
import com.example.notification_service.model.DeliveryAttempt;
import com.example.notification_service.repo.DeliveryAttemptRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
public class PushWorker {

    @Autowired
    private PushNotificationAggregator aggregator;

    @Autowired
    private DeliveryAttemptRepository deliveryAttemptRepository;

    @Autowired
    private NotificationMetrics metrics;

    @KafkaListener(topics = {"notifications-high", "notifications-normal", "notifications-low"})
    public void processPushNotification(NotificationMessage message) {
        if (message.getChannelType() != ChannelType.PUSH) {
            return;
        }

        log.info("Processing push notification: {}", message.getId());

        DeliveryAttempt attempt = DeliveryAttempt.builder()
                .notificationId(message.getId())
                .channelType(ChannelType.PUSH)
                .status(DeliveryStatus.PENDING)
                .attemptNumber(message.getAttemptNumber() + 1)
                .attemptedAt(Instant.now())
                .build();

        try {
            PushNotification pushNotification = PushNotification.builder()
                    .title(message.getTitle())
                    .body(message.getContent())
                    .data(message.getTemplateData())
                    .build();

            aggregator.sendPushNotification(message.getUserId(), pushNotification);

            attempt.setStatus(DeliveryStatus.SENT);
            attempt.setProvider("FCM/APNS");
            attempt.setDeliveredAt(Instant.now());

            metrics.recordDeliverySuccess("FCM/APNS", "push");

        } catch (Exception e) {
            log.error("Failed to send push notification: {}", message.getId(), e);

            attempt.setStatus(DeliveryStatus.FAILED);
            attempt.setErrorMessage(e.getMessage());

            metrics.recordDeliveryFailure("FCM/APNS", "push", e.getClass().getSimpleName());

            if (message.getAttemptNumber() < 5) {
                scheduleRetry(message);
            }
        } finally {
            deliveryAttemptRepository.save(attempt);
        }
    }

    private void scheduleRetry(NotificationMessage message) {
        int nextAttempt = message.getAttemptNumber() + 1;
        long delaySeconds = Math.min(30 * (1L << nextAttempt), 1800); // Max 30 minutes for push

        log.info("Scheduling push retry {} for notification {} in {} seconds",
                nextAttempt, message.getId(), delaySeconds);
    }
}

