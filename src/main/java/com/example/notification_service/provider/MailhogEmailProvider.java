package com.example.notification_service.provider;

import com.example.notification_service.dto.ChannelType;
import com.example.notification_service.dto.DeliveryResult;
import com.example.notification_service.dto.DeliveryStatus;
import com.example.notification_service.dto.NotificationMessage;
import com.example.notification_service.exception.NotificationDeliveryException;
import com.example.notification_service.model.DeliveryAttempt;
import com.example.notification_service.repo.DeliveryAttemptRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage.*;

@Component
public class MailhogEmailProvider implements NotificationProvider {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public String getName() {
        return "mailhog";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public ChannelType getSupportedChannel() {
        return ChannelType.EMAIL;
    }

    @Override
    public DeliveryResult send(NotificationMessage message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            // Get user's email address
            String emailAddress = getUserEmail(message.getUserId());

            helper.setTo(emailAddress);
            helper.setSubject(message.getTitle());
            helper.setText(message.getContent(), true);
            helper.setFrom("noreply@notification-system.local");

            mailSender.send(mimeMessage);

            return DeliveryResult.builder()
                    .success(true)
                    .provider(getName())
                    .messageId(message.getId().toString())
                    .build();

        } catch (Exception e) {
            throw new NotificationDeliveryException("Failed to send email via Mailhog", e);
        }
    }

    private String getUserEmail(String userId) {
        return userId + "@example.com";
    }
}
