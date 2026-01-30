package com.nect.core.entity.user.enums;

public enum Role {
    DESIGNER("디자이너"),
    DEVELOPER("개발자"),
    PLANNER("기획자"),
    MARKETER("마케터"),
    OTHER("기타");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
