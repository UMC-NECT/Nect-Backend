package com.nect.api.domain.user.enums;

public enum CheckType {
    EMAIL("이메일"),
    PHONE("전화번호");

    private final String description;

    CheckType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}