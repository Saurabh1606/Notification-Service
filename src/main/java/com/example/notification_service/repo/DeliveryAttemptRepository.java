package com.example.notification_service.repo;

import com.example.notification_service.dto.ChannelType;
import com.example.notification_service.dto.DeliveryStatus;
import com.example.notification_service.dto.Priority;
import com.example.notification_service.model.DeliveryAttempt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, UUID> {
    List<DeliveryAttempt> findByNotificationIdOrderByAttemptedAtDesc(UUID notificationId);
    List<DeliveryAttempt> findByStatusAndAttemptedAtBefore(DeliveryStatus status, Instant before);

    @Query("SELECT COUNT(da) FROM DeliveryAttempt da WHERE da.provider = :provider AND da.status = :status AND da.attemptedAt > :since")
    Long countByProviderAndStatusSince(@Param("provider") String provider,
                                       @Param("status") DeliveryStatus status,
                                       @Param("since") Instant since);

    boolean existsByNotificationIdAndChannelTypeAndStatus(UUID id, ChannelType channelType, DeliveryStatus deliveryStatus);
}
