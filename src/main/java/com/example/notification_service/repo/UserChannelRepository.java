package com.example.notification_service.repo;

import com.example.notification_service.dto.ChannelType;
import com.example.notification_service.model.UserChannel;
import com.example.notification_service.model.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserChannelRepository extends JpaRepository<UserChannel, UUID> {
    List<UserChannel> findByUserIdAndChannelTypeAndActiveTrue(String userId, ChannelType channelType);
    List<UserChannel> findByUserIdAndActiveTrue(String userId);
}