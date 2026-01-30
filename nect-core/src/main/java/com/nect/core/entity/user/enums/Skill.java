package com.nect.core.entity.user.enums;

public enum Skill {
    // 디자인
    FIGMA("Figma", SkillCategory.DESIGN),
    PHOTOSHOP("Adobe Photoshop", SkillCategory.DESIGN),
    ADOBE_ILLUSTRATOR("Adobe Illustrator", SkillCategory.DESIGN),
    ADOBE_INDESIGN("Adobe InDesign", SkillCategory.DESIGN),
    ADOBE_XD("Adobe XD", SkillCategory.DESIGN),
    ADOBE_PREMIERE_PRO("Adobe Premiere Pro", SkillCategory.DESIGN),
    FINAL_CUT_PRO("Final Cut Pro", SkillCategory.DESIGN),
    ADOBE_AFTER_EFFECT("Adobe After Effect", SkillCategory.DESIGN),
    ADOBE_FIREFLY("Adobe Firefly", SkillCategory.DESIGN),
    ADOBE_LIGHTROOM("Adobe Lightroom", SkillCategory.DESIGN),
    MIDJOURNEY("Midjourney", SkillCategory.DESIGN),
    NANO_BANANA("Nano Banana", SkillCategory.DESIGN),
    DALLE("DALL.E", SkillCategory.DESIGN),
    VFX("VFX", SkillCategory.DESIGN),
    BLENDER("Blender", SkillCategory.DESIGN),
    CINEMA_4D("Cinema 4d", SkillCategory.DESIGN),
    PROCREATE("Procreate", SkillCategory.DESIGN),
    MAYA("MAYA", SkillCategory.DESIGN),
    ZBRUSH("ZBrush", SkillCategory.DESIGN),
    SKETCH_UP("Sketch up", SkillCategory.DESIGN),
    AUTO_CAD("Auto CAD", SkillCategory.DESIGN),
    MAX_3D("3D Max", SkillCategory.DESIGN),
    RHINO("Rhino", SkillCategory.DESIGN),
    KEYSHOT("Keyshot", SkillCategory.DESIGN),
    PROTOPIE("ProtoPie", SkillCategory.DESIGN),
    ENSCAPE("Enscape", SkillCategory.DESIGN),

    // 기술(개발)
    JAVA("Java", SkillCategory.DEVELOPMENT),
    JAVASCRIPT("Java script", SkillCategory.DEVELOPMENT),
    PYTHON("Python", SkillCategory.DEVELOPMENT),
    HTML_CSS("HTML/CSS", SkillCategory.DEVELOPMENT),
    SWIFT("Swift", SkillCategory.DEVELOPMENT),
    CSHARP("C#", SkillCategory.DEVELOPMENT),
    GO("Go", SkillCategory.DEVELOPMENT),
    LUA("Lua", SkillCategory.DEVELOPMENT),
    JSP("JSP", SkillCategory.DEVELOPMENT),
    KOTLIN("Kotlin", SkillCategory.DEVELOPMENT),
    SPRING_BOOT("Spring Boot", SkillCategory.DEVELOPMENT),
    AWS("AWS", SkillCategory.DEVELOPMENT),
    FLUTTER("Flutter", SkillCategory.DEVELOPMENT),
    REACT("React", SkillCategory.DEVELOPMENT),
    SQL("SQL", SkillCategory.DEVELOPMENT),
    REACT_NATIVE("React Native", SkillCategory.DEVELOPMENT),
    NODE_JS("Node.js", SkillCategory.DEVELOPMENT),
    MYSQL("MySQL", SkillCategory.DEVELOPMENT),
    POSTGRESQL("PostgreSQL", SkillCategory.DEVELOPMENT),
    VUE_JS("Vue.js", SkillCategory.DEVELOPMENT),
    SPRING("Spring", SkillCategory.DEVELOPMENT),
    CPLUSPLUS("C++", SkillCategory.DEVELOPMENT),
    DOCKER("Docker", SkillCategory.DEVELOPMENT),
    GIT("Git", SkillCategory.DEVELOPMENT),
    TYPESCRIPT("TypeScript", SkillCategory.DEVELOPMENT),
    GITHUB("GitHub", SkillCategory.DEVELOPMENT),
    ANDROID_STUDIO("Android Studio", SkillCategory.DEVELOPMENT),
    KUBERNETES("Kubernetes", SkillCategory.DEVELOPMENT),
    NEXT_JS("Next.js", SkillCategory.DEVELOPMENT),
    OBJECT_ORIENTED("객체지향", SkillCategory.DEVELOPMENT),

    // 기획
    NOTION("Notion", SkillCategory.PLANNING),
    UX_RESEARCH("UX Research", SkillCategory.PLANNING),
    SLACK("Slack", SkillCategory.PLANNING),
    JIRA("Jira", SkillCategory.PLANNING),
    CONFLUENCE("Confluence", SkillCategory.PLANNING),
    BALSAMIQ("Balsamiq", SkillCategory.PLANNING),
    MIRO("Miro", SkillCategory.PLANNING),
    GOOGLE_ANALYTICS("Google Analytics", SkillCategory.PLANNING),
    AMPLITUDE("Amplitude", SkillCategory.PLANNING),
    EXCEL("Excel", SkillCategory.PLANNING),

    // 마케팅
    GOOGLE_TAG_MANAGER("Google Tag Manager", SkillCategory.MARKETING),
    APPSFLYER("AppsFlyer", SkillCategory.MARKETING),
    META_ADS_MANAGER("Meta Ads Manager", SkillCategory.MARKETING),
    GOOGLE_ADS("Google Ads", SkillCategory.MARKETING),
    KAKAO_MOMENT("Kakao Moment", SkillCategory.MARKETING),
    CANVA("Canva", SkillCategory.MARKETING),
    BRAZE("Braze", SkillCategory.MARKETING),
    SOLAPI("Solapi", SkillCategory.MARKETING),
    MAILCHIMP("Mailchimp", SkillCategory.MARKETING),

    // 기타
    CHATGPT("Chat GPT", SkillCategory.OTHER),
    GOOGLE_GEMINI("Google Gemini", SkillCategory.OTHER),
    CLAUDE("Claude", SkillCategory.OTHER),
    CONSECUTIVE_INTERPRETATION("Consecutive Interpretation", SkillCategory.OTHER),
    GOOGLE_WORKSPACE("Google Workspace", SkillCategory.OTHER),
    PRO_TOOLS("Pro Tools", SkillCategory.OTHER),
    MICANVAS("Micanvas", SkillCategory.OTHER),
    ABLETON_LIVE("Ableton Live", SkillCategory.OTHER),
    CHANNEL_TALK("Channel Talk", SkillCategory.OTHER),
    IZOTOPE_RX("iZotope RX", SkillCategory.OTHER),
    CAPCUT("CapCut", SkillCategory.OTHER),
    DAVINCI_RESOLVE("DaVinci Resolve", SkillCategory.OTHER),
    VIEW("View", SkillCategory.OTHER),
    STORYBOARDING("Storyboarding", SkillCategory.OTHER),
    DEEPL("DeepL", SkillCategory.OTHER),
    DRONE_PILOTING("Drone Piloting", SkillCategory.OTHER),
    VREW("Vrew", SkillCategory.OTHER),
    LOGIC_PRO("Logic Pro", SkillCategory.OTHER),
    POWER_BI("Power BI", SkillCategory.OTHER),
    CRM_SETUP("CRM Setup", SkillCategory.OTHER),
    TRADOS("Trados", SkillCategory.OTHER),
    LIVE_STREAMING("Live Streaming", SkillCategory.OTHER),
    MEMISOURCE("Memisource", SkillCategory.OTHER),
    VMD("VMD", SkillCategory.OTHER),
    THREE_D_RENDERING("3D Rendering", SkillCategory.OTHER),
    SAFETY_MANAGEMENT("Safety Management", SkillCategory.OTHER),

    // 직접입력
    CUSTOM("직접입력", SkillCategory.OTHER);

    private final String displayName;
    private final SkillCategory category;

    Skill(String displayName, SkillCategory category) {
        this.displayName = displayName;
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public SkillCategory getCategory() {
        return category;
    }
}
