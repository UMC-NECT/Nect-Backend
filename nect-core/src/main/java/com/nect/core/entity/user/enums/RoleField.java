package com.nect.core.entity.user.enums;

public enum RoleField {
    // 디자이너
    UI_UX("UI/UX", Role.DESIGNER),
    ILLUSTRATION_GRAPHIC("일러스트/그래픽", Role.DESIGNER),
    WEBTOON_EMOTICON("웹툰/이모티콘", Role.DESIGNER),
    PHOTO_VIDEO("사진/영상", Role.DESIGNER),
    SOUND("사운드", Role.DESIGNER),
    THREE_D_MOTION("3D/모션", Role.DESIGNER),
    PRODUCT("제품", Role.DESIGNER),
    SPACE("공간", Role.DESIGNER),
    PUBLISHING("출판", Role.DESIGNER),

    // 개발자
    FRONTEND("프론트엔드", Role.DEVELOPER),
    BACKEND("백엔드", Role.DEVELOPER),
    IOS_ANDROID("IOS/안드로이드", Role.DEVELOPER),
    DATA_ENGINEER("데이터 엔지니어", Role.DEVELOPER),
    AI_MACHINE_LEARNING("AI/머신러닝", Role.DEVELOPER),
    FULLSTACK("풀스택", Role.DEVELOPER),
    GAME("게임", Role.DEVELOPER),
    HARDWARE("하드웨어", Role.DEVELOPER),
    SECURITY_NETWORK("보안/네트워크", Role.DEVELOPER),

    // 기획자
    SERVICE("서비스", Role.PLANNER),
    UX("UX", Role.PLANNER),
    APP_WEB("앱/웹", Role.PLANNER),
    BUSINESS("비즈니스", Role.PLANNER),
    PERFORMANCE_EVENT("공연/행사", Role.PLANNER),

    // 마케터
    CONTENT_CREATION("콘텐츠 제작", Role.MARKETER),
    PERFORMANCE("퍼포먼스", Role.MARKETER),
    CRM("CRM", Role.MARKETER),
    BRAND_MARKETING("브랜드 마케팅", Role.MARKETER),
    AD_VIRAL("광고/바이럴", Role.MARKETER),
    LIVE_COMMERCE("라이브커머스", Role.MARKETER),
    DATA_ANALYSIS("데이터 분석", Role.MARKETER),
    MARKETING_OTHER("기타", Role.MARKETER),
    OPERATIONS_CS("운영/CS", Role.MARKETER),
    SALES_PARTNERSHIP("영업/제휴", Role.MARKETER),
    VIDEO_MUSIC_DIRECTING("영상/음악 감독", Role.MARKETER),
    TRANSLATION_INTERPRETATION("번역/통역", Role.MARKETER),
    MANUSCRIPT_CONSULTING("원고 컨설턴트", Role.MARKETER),
    ACCOUNTING_LAW_HR("세무/법무/노무", Role.MARKETER),
    STARTUP_CONSULTING("창업 컨설팅", Role.MARKETER),

    // 직접입력 (모든 Role에서 가능)
    CUSTOM("직접입력", null);

    private final String description;
    private final Role role;

    RoleField(String description, Role role) {
        this.description = description;
        this.role = role;
    }

    public String getDescription() {
        return description;
    }

    public Role getRole() {
        return role;
    }

    /**
     * Role에 따른 필드 필터링
     * @param role 필터링할 Role
     * @return 해당 Role에 속하는 RoleField 배열
     */
    public static RoleField[] getFieldsByRole(Role role) {
        return java.util.Arrays.stream(RoleField.values())
                .filter(field -> field.role == null || field.role.equals(role))
                .toArray(RoleField[]::new);
    }
}