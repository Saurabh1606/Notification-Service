package com.example.notification_service.provider;

import com.example.notification_service.dto.ChannelType;
import com.example.notification_service.dto.DeliveryResult;
import com.example.notification_service.dto.DeliveryStatus;
import com.example.notification_service.dto.NotificationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MockPushProvider implements NotificationProvider {

    @Override
    public String getName() {
        return "mock-push";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public ChannelType getSupportedChannel() {
        return ChannelType.PUSH;
    }

    @Override
    public DeliveryResult send(NotificationMessage message) {

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return DeliveryResult.builder()
                .success(true)
                .provider(getName())
                .messageId(message.getId().toString())
                .build();
    }
}
