package com.example.notification_service.service;

// ===== UserPreferenceService.java =====

import com.example.notification_service.dto.ChannelType;
import com.example.notification_service.model.UserChannel;
import com.example.notification_service.model.UserPreference;
import com.example.notification_service.repo.UserChannelRepository;
import com.example.notification_service.repo.UserPreferenceRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@Slf4j
public class UserPreferenceService {

    @Autowired
    private UserPreferenceRepository preferenceRepository;

    @Autowired
    private UserChannelRepository channelRepository;

    public UserPreference getUserPreference(String userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("Creating default preferences for user: {}", userId);
                    UserPreference defaultPref = UserPreference.builder()
                            .userId(userId)
                            .emailEnabled(true)
                            .pushEnabled(true)
                            .smsEnabled(false)
                            .timezone("UTC")
                            .build();
                    return preferenceRepository.save(defaultPref);
                });
    }

    public UserPreference updateUserPreference(UserPreference preference) {
        return preferenceRepository.save(preference);
    }

    public UserChannel addUserChannel(UserChannel channel) {
        return channelRepository.save(channel);
    }

    public List<UserChannel> getUserChannels(String userId) {
        return channelRepository.findByUserIdAndActiveTrue(userId);
    }

    public List<UserChannel> getVerifiedChannels(String userId, ChannelType channelType) {
        return channelRepository.findByUserIdAndChannelTypeAndActiveTrue(userId, channelType)
                .stream()
                .filter(UserChannel::getVerified)
                .toList();
    }
}

