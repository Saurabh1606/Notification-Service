-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- User preferences table
CREATE TABLE IF NOT EXISTS user_preferences (
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
CREATE TABLE IF NOT EXISTS user_channels (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id VARCHAR(255) NOT NULL,
    channel_type VARCHAR(10) CHECK (channel_type IN ('EMAIL', 'PUSH', 'SMS')),
    address VARCHAR(255),
    verified BOOLEAN DEFAULT false,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notification categories table
CREATE TABLE IF NOT EXISTS notification_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) UNIQUE,
    description TEXT
);

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id VARCHAR(255) NOT NULL,
    title VARCHAR(255),
    content TEXT,
    priority VARCHAR(10) DEFAULT 'NORMAL' CHECK (priority IN ('HIGH', 'NORMAL', 'LOW')),
    category VARCHAR(20) DEFAULT 'SYSTEM' CHECK (category IN ('SECURITY', 'MARKETING', 'SYSTEM', 'REMINDER', 'ALERT')),
    scheduled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    idempotency_key VARCHAR(255) UNIQUE
);

-- Delivery attempts table
CREATE TABLE IF NOT EXISTS delivery_attempts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    notification_id UUID NOT NULL,
    channel_type VARCHAR(10) CHECK (channel_type IN ('EMAIL', 'PUSH', 'SMS')),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'BOUNCED', 'CANCELLED')),
    provider VARCHAR(50),
    attempt_number INTEGER DEFAULT 1,
    error_message TEXT,
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP,
    FOREIGN KEY (notification_id) REFERENCES notifications(id) ON DELETE CASCADE
);

-- Scheduled notifications table (for future delivery)
CREATE TABLE IF NOT EXISTS scheduled_notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    notification_data JSONB NOT NULL,
    scheduled_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_user_preferences_user_id ON user_preferences(user_id);
CREATE INDEX IF NOT EXISTS idx_user_channels_user_id_type ON user_channels(user_id, channel_type);
CREATE INDEX IF NOT EXISTS idx_user_channels_active ON user_channels(active);
CREATE INDEX IF NOT EXISTS idx_notifications_user_created ON notifications(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_scheduled ON notifications(scheduled_at);
CREATE INDEX IF NOT EXISTS idx_notifications_priority ON notifications(priority);
CREATE INDEX IF NOT EXISTS idx_delivery_attempts_notification ON delivery_attempts(notification_id);
CREATE INDEX IF NOT EXISTS idx_delivery_attempts_status ON delivery_attempts(status, attempted_at);
CREATE INDEX IF NOT EXISTS idx_delivery_attempts_provider ON delivery_attempts(provider, attempted_at);
CREATE INDEX IF NOT EXISTS idx_scheduled_notifications_time ON scheduled_notifications(scheduled_time);

-- Insert default notification categories
INSERT INTO notification_categories (name, description) VALUES
('SECURITY', 'Security-related notifications'),
('MARKETING', 'Marketing and promotional messages'),
('SYSTEM', 'System notifications and updates'),
('REMINDER', 'Reminder notifications'),
('ALERT', 'Important alerts and warnings')
ON CONFLICT (name) DO NOTHING;

-- Insert sample users for testing
INSERT INTO user_preferences (user_id, email_enabled, push_enabled, sms_enabled, timezone) VALUES
('user1', true, true, false, 'America/New_York'),
('user2', true, false, true, 'Europe/London'),
('user3', true, true, true, 'Asia/Tokyo'),
('testuser', true, true, false, 'UTC')
ON CONFLICT (user_id) DO NOTHING;

-- Insert sample user channels
INSERT INTO user_channels (user_id, channel_type, address, verified, active) VALUES
('user1', 'EMAIL', 'user1@example.com', true, true),
('user1', 'PUSH', 'device_token_user1_android', true, true),
('user2', 'EMAIL', 'user2@example.com', true, true),
('user2', 'SMS', '+1234567890', true, true),
('user3', 'EMAIL', 'user3@example.com', true, true),
('user3', 'PUSH', 'device_token_user3_ios', true, true),
('user3', 'SMS', '+0987654321', true, true),
('testuser', 'EMAIL', 'testuser@example.com', true, true)
ON CONFLICT DO NOTHING;
