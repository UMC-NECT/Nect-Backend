package com.nect.core.entity.user.enums;

public enum InterestField {
    IT_WEB_MOBILE("IT·웹/모바일 서비스"),
    PUBLISHING_CONTENT("출판·콘텐츠 제작"),
    ART_DIGITAL_MEDIA("예술·전자미디어아트"),
    HEALTHCARE_FITNESS("헬스케어·피트니스"),
    EDUCATION_EDUTECH("교육·에듀테크"),
    FINANCE_FINTECH("금융·핀테크"),
    GAME_ENTERTAINMENT("게임·엔터테인먼트"),
    OTHER("기타");

    private final String description;

    InterestField(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
