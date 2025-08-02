package com.example.notification_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "user_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {
    @Id
    private String userId;

    @Column(name = "email_enabled")
    private Boolean emailEnabled = true;

    @Column(name = "push_enabled")
    private Boolean pushEnabled = true;

    @Column(name = "sms_enabled")
    private Boolean smsEnabled = false;

    @Column(name = "muted_until")
    private Instant mutedUntil;

    @Column(name = "timezone")
    private String timezone = "UTC";

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
