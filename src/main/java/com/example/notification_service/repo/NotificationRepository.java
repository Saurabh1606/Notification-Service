package com.example.notification_service.repo;

import com.example.notification_service.dto.Priority;
import com.example.notification_service.model.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByScheduledAtLessThanEqual(Instant scheduledAt);
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    List<Notification> findByPriorityAndScheduledAtLessThanEqual(Priority priority, Instant scheduledAt);
}
