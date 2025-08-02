package com.example.notification_service.service;

import com.example.notification_service.dto.DeliveryResult;
import com.example.notification_service.dto.NotificationMessage;
import com.example.notification_service.exception.NotificationDeliveryException;
import com.example.notification_service.provider.NotificationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MultiProviderFailoverService {

    @Autowired
    private List<NotificationProvider> allProviders;

    public DeliveryResult sendWithFailover(NotificationMessage message) {
        List<NotificationProvider> providers = allProviders.stream()
                .filter(p -> p.getSupportedChannel() == message.getChannelType())
                .filter(NotificationProvider::isAvailable)
                .toList();

        for (NotificationProvider provider : providers) {
            try {
                return provider.send(message);
            } catch (Exception e) {
                // log but try next
            }
        }

        throw new NotificationDeliveryException("All providers failed for " + message.getChannelType());
    }
}

