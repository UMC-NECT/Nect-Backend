package com.nect.core.entity.user.enums;

public enum UserStatus {
    ENROLLED("재학중"),
    JOB_SEEKING("구직중"),
    EMPLOYED("재직중");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}