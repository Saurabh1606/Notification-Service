package com.example.notification_service.service;

// ===== ScheduledNotificationDispatcher.java =====

import com.example.notification_service.dto.ChannelType;
import com.example.notification_service.dto.NotificationMessage;
import com.example.notification_service.model.Notification;
import com.example.notification_service.model.UserPreference;
import com.example.notification_service.repo.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

import static com.example.notification_service.dto.ChannelType.SMS;

@Service
@Slf4j
public class ScheduledNotificationDispatcher {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserPreferenceService userPreferenceService;

    @Autowired
    private PriorityQueueManager queueManager;

    @Autowired
    private IdempotencyService idempotencyService;

    @Scheduled(fixedRate = 60000)
    public void dispatchScheduledNotifications() {
        Instant now = Instant.now();
        List<Notification> dueNotifications = notificationRepository.findByScheduledAtLessThanEqual(now);

        for (Notification notification : dueNotifications) {
            try {
                UserPreference pref = userPreferenceService.getUserPreference(notification.getUserId());
                if (pref.getMutedUntil() != null && pref.getMutedUntil().isAfter(now)) {
                    log.info("Skipping muted notification for user {}", notification.getUserId());
                    continue;
                }

                for (ChannelType channel : ChannelType.values()) {
                    if (isChannelEnabled(pref, channel)) {
                        NotificationMessage message = NotificationMessage.builder()
                                .id(notification.getId())
                                .userId(notification.getUserId())
                                .title(notification.getTitle())
                                .content(notification.getContent())
                                .priority(notification.getPriority())
                                .category(notification.getCategory())
                                .channelType(channel)
                                .scheduledAt(notification.getScheduledAt())
                                .idempotencyKey(notification.getIdempotencyKey())
                                .attemptNumber(0)
                                .build();

                        queueManager.sendNotification(message);
                    }
                }

                idempotencyService.markAsProcessed(notification.getIdempotencyKey(), notification.getId().toString());

            } catch (Exception e) {
                log.error("Failed to dispatch scheduled notification: {}", notification.getId(), e);
            }
        }
    }

    private boolean isChannelEnabled(UserPreference pref, ChannelType channel) {
        return switch (channel) {
            case EMAIL -> Boolean.TRUE.equals(pref.getEmailEnabled());
            case PUSH -> Boolean.TRUE.equals(pref.getPushEnabled());
            case SMS -> Boolean.TRUE.equals(pref.getSmsEnabled());
        };
    }
}
