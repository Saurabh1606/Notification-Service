package com.example.notification_service.service;

import com.example.notification_service.dto.NotificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class IdempotencyService {

    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public boolean isProcessed(String idempotencyKey) {
        String key = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void markAsProcessed(String idempotencyKey, String notificationId) {
        String key = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        redisTemplate.opsForValue().set(key, notificationId, Duration.ofHours(24)); // 1-day expiry
    }

    public String generateIdempotencyKey(NotificationRequest request) {
        String raw = request.getUserId() + ":" + request.getTitle() + ":" + request.getContent();
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }
}
