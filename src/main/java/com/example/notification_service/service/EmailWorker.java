package com.example.notification_service.service;

import com.example.notification_service.dto.ChannelType;
import com.example.notification_service.dto.DeliveryResult;
import com.example.notification_service.dto.DeliveryStatus;
import com.example.notification_service.dto.NotificationMessage;
import com.example.notification_service.model.DeliveryAttempt;
import com.example.notification_service.repo.DeliveryAttemptRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
public class EmailWorker {

    @Autowired
    private MultiProviderFailoverService failoverService;

    @Autowired
    private DeliveryAttemptRepository deliveryAttemptRepository;

    @Autowired
    private NotificationMetrics metrics;

    @KafkaListener(topics = {"notifications-high", "notifications-normal", "notifications-low"})
    public void processEmailNotification(NotificationMessage message) {
        if (message.getChannelType() != ChannelType.EMAIL) {
            return;
        }

        log.info("Processing email notification: {}", message.getId());

        DeliveryAttempt attempt = DeliveryAttempt.builder()
                .notificationId(message.getId())
                .channelType(ChannelType.EMAIL)
                .status(DeliveryStatus.PENDING)
                .attemptNumber(message.getAttemptNumber() + 1)
                .attemptedAt(Instant.now())
                .build();

        try {
            boolean alreadySent = deliveryAttemptRepository.existsByNotificationIdAndChannelTypeAndStatus(
                    message.getId(), ChannelType.EMAIL, DeliveryStatus.SENT);

            if (alreadySent) {
                log.info("Skipping already processed email: {}", message.getId());
                return;
            }
            DeliveryResult result = failoverService.sendWithFailover(message);

            attempt.setStatus(DeliveryStatus.SENT);
            attempt.setProvider(result.getProvider());
            attempt.setDeliveredAt(Instant.now());

            metrics.recordDeliverySuccess(result.getProvider(), "email");

        } catch (Exception e) {
            log.error("Failed to send email notification: {}", message.getId(), e);

            attempt.setStatus(DeliveryStatus.FAILED);
            attempt.setErrorMessage(e.getMessage());

            metrics.recordDeliveryFailure("unknown", "email", e.getClass().getSimpleName());

            // Schedule retry if within limits
            if (message.getAttemptNumber() < 10) {
                scheduleRetry(message);
            }
        } finally {
            deliveryAttemptRepository.save(attempt);
        }
    }

    private void scheduleRetry(NotificationMessage message) {

        int nextAttempt = message.getAttemptNumber() + 1;
        long delaySeconds = Math.min(30 * (1L << nextAttempt), 3600);

        log.info("Scheduling retry {} for notification {} in {} seconds",
                nextAttempt, message.getId(), delaySeconds);
    }
}

