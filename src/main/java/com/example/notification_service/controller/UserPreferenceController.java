package com.example.notification_service.controller;

import com.example.notification_service.dto.NotificationRequest;
import com.example.notification_service.dto.NotificationResponse;
import com.example.notification_service.model.DeliveryAttempt;
import com.example.notification_service.model.Notification;
import com.example.notification_service.model.UserChannel;
import com.example.notification_service.model.UserPreference;
import com.example.notification_service.service.UserPreferenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/preferences")
@Slf4j
public class UserPreferenceController {

    @Autowired
    private UserPreferenceService userPreferenceService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserPreference> getUserPreferences(@PathVariable String userId) {
        UserPreference preference = userPreferenceService.getUserPreference(userId);
        return ResponseEntity.ok(preference);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserPreference> updateUserPreferences(
            @PathVariable String userId,
            @RequestBody UserPreference preference) {
        preference.setUserId(userId);
        UserPreference updated = userPreferenceService.updateUserPreference(preference);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{userId}/channels")
    public ResponseEntity<UserChannel> addUserChannel(
            @PathVariable String userId,
             @RequestBody UserChannel channel) {
        channel.setUserId(userId);
        UserChannel saved = userPreferenceService.addUserChannel(channel);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{userId}/channels")
    public ResponseEntity<List<UserChannel>> getUserChannels(@PathVariable String userId) {
        List<UserChannel> channels = userPreferenceService.getUserChannels(userId);
        return ResponseEntity.ok(channels);
    }
}

