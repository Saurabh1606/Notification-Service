package com.example.notification_service.provider;

import com.example.notification_service.dto.ChannelType;
import com.example.notification_service.dto.DeliveryResult;
import com.example.notification_service.dto.DeliveryStatus;
import com.example.notification_service.dto.NotificationMessage;
import com.example.notification_service.exception.NotificationDeliveryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class MockSmsProvider implements NotificationProvider {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${notification.providers.sms.mock.endpoint}")
    private String mockEndpoint;

    @Override
    public String getName() {
        return "mock-sms";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public ChannelType getSupportedChannel() {
        return ChannelType.SMS;
    }

    @Override
    public DeliveryResult send(NotificationMessage message) {
        try {
            Map<String, Object> smsRequest = Map.of(
                    "to", getUserPhoneNumber(message.getUserId()),
                    "message", message.getContent(),
                    "from", "NotificationSystem"
            );

            ResponseEntity<String> response = restTemplate.postForEntity(
                    mockEndpoint, smsRequest, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return DeliveryResult.builder()
                        .success(true)
                        .provider(getName())
                        .messageId(message.getId().toString())
                        .build();
            } else {
                throw new NotificationDeliveryException("Mock SMS service returned: " + response.getStatusCode());
            }

        } catch (Exception e) {
            throw new NotificationDeliveryException("Failed to send SMS via Mock provider", e);
        }
    }

    private String getUserPhoneNumber(String userId) {
        // In real implementation, fetch from user service
        return "+1234567890";
    }
}
