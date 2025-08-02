package com.example.notification_service.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

@Component
public class NotificationMetrics {

    private final Map<String, LongAdder> counters = new ConcurrentHashMap<>();

    public void recordDeliverySuccess(String provider, String channel) {
        getCounter(provider + ":" + channel + ":success").increment();
    }

    public void recordDeliveryFailure(String provider, String channel, String errorType) {
        getCounter(provider + ":" + channel + ":fail:" + errorType).increment();
    }

    private LongAdder getCounter(String key) {
        return counters.computeIfAbsent(key, k -> new LongAdder());
    }

    public Map<String, Long> getMetricsSnapshot() {
        return counters.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().longValue()));
    }
}

