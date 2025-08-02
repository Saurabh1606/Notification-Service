package com.example.notification_service.dto;

public enum Priority {
    HIGH(1), NORMAL(2), LOW(3);

    private final int value;

    Priority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
