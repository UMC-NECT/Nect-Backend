package com.nect.core.entity.notifications.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * 알림 분류입니다 입니다.
 * 알림 상단부에 표기됩니다. ( QA 페이지 참고 )
 *
 */
@Getter
@AllArgsConstructor
public enum NotificationClassification {

    // 홈 전용
    MY_PAGE("마이페이지"),
    MESSAGE("메시지"),
    WISH_LIST("위시리스트"),

    // 홈 & 작업실 공용 -> 작업실명으로 표시
    WORK_SPACE(null),

    // 작업실 전용
    FILE_UPlOAD("파일 업로드"),
    BOARD("게시판"),
    WORK_STATUS("작업현황"),
    WEEK_MISSION("위크미션")

    ;

    private final String classifyKr;


}