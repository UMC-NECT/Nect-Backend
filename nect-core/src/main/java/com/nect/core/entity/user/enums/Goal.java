package com.nect.core.entity.user.enums;

public enum Goal {
    PORTFOLIO("포트폴리오 제작"),
    TEAM_EXPERIENCE("프로젝트 경험 및 팀 협업 능력 향상"),
    OTHER_FIELD("다른 분야의 프로젝트 경험"),
    ETC("기타");

    private final String description;

    Goal(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
