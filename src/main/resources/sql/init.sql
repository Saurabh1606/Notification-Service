CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- User preferences table
CREATE TABLE user_preferences (
    user_id VARCHAR(255) PRIMARY KEY,
    email_enabled BOOLEAN DEFAULT true,
    push_enabled BOOLEAN DEFAULT true,
    sms_enabled BOOLEAN DEFAULT false,
    muted_until TIMESTAMP,
    timezone VARCHAR(50) DEFAULT 'UTC',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User channels table
CREATE TABLE user_channels (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id VARCHAR(255) NOT NULL,
    channel_type VARCHAR(10) CHECK (channel_type IN ('EMAIL', 'PUSH', 'SMS')),
    address VARCHAR(255),
    verified BOOLEAN DEFAULT false,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES user_preferences(user_id)
);

-- Notifications table
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id VARCHAR(255) NOT NULL,
    title VARCHAR(255),
    content TEXT,
    priority VARCHAR(10) DEFAULT 'NORMAL' CHECK (priority IN ('HIGH', 'NORMAL', 'LOW')),
    category VARCHAR(20) DEFAULT 'SYSTEM' CHECK (category IN ('SECURITY', 'MARKETING', 'SYSTEM', 'REMINDER', 'ALERT')),
    scheduled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    idempotency_key VARCHAR(255) UNIQUE,

    FOREIGN KEY (user_id) REFERENCES user_preferences(user_id)
);

-- Delivery attempts table
CREATE TABLE delivery_attempts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    notification_id UUID NOT NULL,
    channel_type VARCHAR(10) CHECK (channel_type IN ('EMAIL', 'PUSH', 'SMS')),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'BOUNCED', 'CANCELLED')),
    provider VARCHAR(50),
    attempt_number INTEGER DEFAULT 1,
    error_message TEXT,
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP,

    FOREIGN KEY (notification_id) REFERENCES notifications(id)
);

-- Indexes for performance
CREATE INDEX idx_user_preferences_user_id ON user_preferences(user_id);
CREATE INDEX idx_user_channels_user_id_type ON user_channels(user_id, channel_type);
CREATE INDEX idx_notifications_user_created ON notifications(user_id, created_at);
CREATE INDEX idx_notifications_scheduled ON notifications(scheduled_at);
CREATE INDEX idx_delivery_attempts_notification ON delivery_attempts(notification_id);
CREATE INDEX idx_delivery_attempts_status ON delivery_attempts(status, attempted_at);

-- Sample data
INSERT INTO user_preferences (user_id, email_enabled, push_enabled, sms_enabled) VALUES
('user1', true, true, false),
('user2', true, false, true),
('user3', true, true, true);

INSERT INTO user_channels (user_id, channel_type, address, verified, active) VALUES
('user1', 'EMAIL', 'user1@example.com', true, true),
('user1', 'PUSH', 'device_token_1', true, true),
('user2', 'EMAIL', 'user2@example.com', true, true),
('user2', 'SMS', '+1234567890', true, true),
('user3', 'EMAIL', 'user3@example.com', true, true),
('user3', 'PUSH', 'device_token_3', true, true),
('user3', 'SMS', '+0987654321', true, true);