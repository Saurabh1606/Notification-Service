package com.example.notification_service.provider;

import com.example.notification_service.dto.ChannelType;
import com.example.notification_service.dto.DeliveryResult;
import com.example.notification_service.dto.NotificationMessage;

public interface NotificationProvider {
    String getName();
    boolean isAvailable();
    DeliveryResult send(NotificationMessage message);
    ChannelType getSupportedChannel();
}
