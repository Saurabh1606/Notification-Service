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
public class SmsWorker {

    @Autowired
    private MultiProviderFailoverService failoverService;

    @Autowired
    private DeliveryAttemptRepository deliveryAttemptRepository;

    @Autowired
    private NotificationMetrics metrics;

    @KafkaListener(topics = {"notifications-high", "notifications-normal", "notifications-low"})
    public void processSmsNotification(NotificationMessage message) {
        if (message.getChannelType() != ChannelType.SMS) {
            return;
        }

        log.info("Processing SMS notification: {}", message.getId());

        DeliveryAttempt attempt = DeliveryAttempt.builder()
                .notificationId(message.getId())
                .channelType(ChannelType.SMS)
                .status(DeliveryStatus.PENDING)
                .attemptNumber(message.getAttemptNumber() + 1)
                .attemptedAt(Instant.now())
                .build();

        try {
            DeliveryResult result = failoverService.sendWithFailover(message);

            attempt.setStatus(DeliveryStatus.SENT);
            attempt.setProvider(result.getProvider());
            attempt.setDeliveredAt(Instant.now());

            metrics.recordDeliverySuccess(result.getProvider(), "sms");

        } catch (Exception e) {
            log.error("Failed to send SMS notification: {}", message.getId(), e);

            attempt.setStatus(DeliveryStatus.FAILED);
            attempt.setErrorMessage(e.getMessage());

            metrics.recordDeliveryFailure("unknown", "sms", e.getClass().getSimpleName());

            if (message.getAttemptNumber() < 3) { // Fewer retries for SMS due to cost
                scheduleRetry(message);
            }
        } finally {
            deliveryAttemptRepository.save(attempt);
        }
    }

    private void scheduleRetry(NotificationMessage message) {
        int nextAttempt = message.getAttemptNumber() + 1;
        long delaySeconds = Math.min(60 * (1L << nextAttempt), 7200); // Max 2 hours for SMS

        log.info("Scheduling SMS retry {} for notification {} in {} seconds",
                nextAttempt, message.getId(), delaySeconds);
    }
}