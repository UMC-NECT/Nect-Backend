package com.nect.core.entity.user.enums;

public enum RoleField {
    // 디자이너
    UI_UX("UI/UX", "UI/UX", Role.DESIGNER),
    ILLUSTRATION_GRAPHIC("일러스트/그래픽", "Illustration/Graphic", Role.DESIGNER),
    WEBTOON_EMOTICON("웹툰/이모티콘", "Webtoon/Emoticon", Role.DESIGNER),
    PHOTO_VIDEO("사진/영상", "Photo/Video", Role.DESIGNER),
    SOUND("사운드", "Sound", Role.DESIGNER),
    THREE_D_MOTION("3D/모션", "3D/Motion", Role.DESIGNER),
    PRODUCT("제품", "Product", Role.DESIGNER),
    SPACE("공간", "Space", Role.DESIGNER),
    PUBLISHING("출판", "Publishing", Role.DESIGNER),

    // 개발자
    FRONTEND("프론트엔드", "Frontend", Role.DEVELOPER),
    BACKEND("백엔드", "Backend", Role.DEVELOPER),
    IOS_ANDROID("IOS/안드로이드", "iOS/Android", Role.DEVELOPER),
    DATA_ENGINEER("데이터 엔지니어", "Data Engineer", Role.DEVELOPER),
    AI_MACHINE_LEARNING("AI/머신러닝", "AI/Machine Learning", Role.DEVELOPER),
    FULLSTACK("풀스택", "Full-stack", Role.DEVELOPER),
    GAME("게임", "Game", Role.DEVELOPER),
    HARDWARE("하드웨어", "Hardware", Role.DEVELOPER),
    SECURITY_NETWORK("보안/네트워크", "Security/Network", Role.DEVELOPER),

    // 기획자
    SERVICE("서비스", "Service", Role.PLANNER),
    UX("UX", "UX", Role.PLANNER),
    APP_WEB("앱/웹", "App/Web", Role.PLANNER),
    BUSINESS("비즈니스", "Business", Role.PLANNER),
    PERFORMANCE_EVENT("공연/행사", "Performance/Event", Role.PLANNER),


    // 마케터
    CONTENT_CREATION("콘텐츠 제작", "Content Creation", Role.MARKETER),
    PERFORMANCE("퍼포먼스", "Performance", Role.MARKETER),
    CRM("CRM", "CRM", Role.MARKETER),
    BRAND_MARKETING("브랜드 마케팅", "Brand Marketing", Role.MARKETER),
    AD_VIRAL("광고/바이럴", "Ads/Viral", Role.MARKETER),
    LIVE_COMMERCE("라이브커머스", "Live Commerce", Role.MARKETER),
    DATA_ANALYSIS("데이터 분석", "Data Analysis", Role.MARKETER),
    MARKETING_OTHER("기타", "Other", Role.MARKETER),
    OPERATIONS_CS("운영/CS", "Operations/CS", Role.MARKETER),
    SALES_PARTNERSHIP("영업/제휴", "Sales/Partnership", Role.MARKETER),
    VIDEO_MUSIC_DIRECTING("영상/음악 감독", "Video/Music Directing", Role.MARKETER),
    TRANSLATION_INTERPRETATION("번역/통역", "Translation/Interpretation", Role.MARKETER),
    MANUSCRIPT_CONSULTING("원고 컨설턴트", "Manuscript Consulting", Role.MARKETER),
    ACCOUNTING_LAW_HR("세무/법무/노무", "Accounting/Law/HR", Role.MARKETER),
    STARTUP_CONSULTING("창업 컨설팅", "Startup Consulting", Role.MARKETER),

    // 직접입력 (모든 Role에서 가능)
    CUSTOM("직접입력", "Custom",null);

    private final String description;
    private final String labelEn;
    private final Role role;

    RoleField(String description, String labelEn, Role role) {
        this.description = description;
        this.labelEn = labelEn;
        this.role = role;
    }

    public String getDescription() {
        return description;
    }

    public String getLabelEn() {
        return labelEn;
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