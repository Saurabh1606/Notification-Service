package com.example.notification_service.controller;

import com.example.notification_service.dto.NotificationRequest;
import com.example.notification_service.dto.NotificationResponse;
import com.example.notification_service.model.DeliveryAttempt;
import com.example.notification_service.model.Notification;
import com.example.notification_service.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@Validated
@Slf4j
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(
             @RequestBody NotificationRequest request) {
        try {
            NotificationResponse response = notificationService.processNotification(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to process notification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(NotificationResponse.builder()
                            .status("ERROR")
                            .message("Failed to process notification: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> sendBulkNotifications(
             @RequestBody List<NotificationRequest> requests) {
        try {
            List<NotificationResponse> responses = notificationService.processBulkNotifications(requests);

            Map<String, Object> result = new HashMap<>();
            result.put("total", requests.size());
            result.put("successful", responses.stream().mapToInt(r -> "SUCCESS".equals(r.getStatus()) ? 1 : 0).sum());
            result.put("responses", responses);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to process bulk notifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process bulk notifications: " + e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        List<Notification> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{notificationId}/status")
    public ResponseEntity<List<DeliveryAttempt>> getNotificationStatus(
            @PathVariable UUID notificationId) {
        List<DeliveryAttempt> attempts = notificationService.getDeliveryAttempts(notificationId);
        return ResponseEntity.ok(attempts);
    }
}