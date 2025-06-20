package com.example.baitapcuoiki2.utils;

public enum Status {
    INACTIVE(0),
    ACTIVE(1);

    private final int value;

    Status(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Status fromValue(int value) {
        for (Status status : Status.values()) {
            if (status.value == value) return status;
        }
        throw new IllegalArgumentException("Invalid Status value: " + value);
    }
}
