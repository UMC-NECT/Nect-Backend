package com.nect.core.entity.user.enums;

public enum SkillCategory {
    DESIGN("디자인"),
    PLANNING("기획"),
    DEVELOPMENT("개발"),
    MARKETING("마케팅"),
    OTHER("기타");

    private final String description;

    SkillCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
