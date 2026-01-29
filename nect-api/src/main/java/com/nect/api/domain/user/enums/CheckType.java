package com.nect.api.domain.user.enums;

public enum CheckType {
    EMAIL("이메일"),
    PHONE("전화번호"),
    NICKNAME("닉네임");

    private final String description;

    CheckType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}