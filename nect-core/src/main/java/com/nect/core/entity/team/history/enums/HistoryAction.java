package com.nect.core.entity.team.history.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.IllegalFormatException;

@Getter
@AllArgsConstructor
public enum HistoryAction {

    // 위크미션(리더형 모달) 블록 수정
    WEEK_MISSION_STATUS_CHANGED(
            "위크미션 Mission %d의 상태가 수정되었습니다.",
            "“%s”"
    ),

    WEEK_MISSION_TASK_ITEM_UPDATED(
            "위크미션 Mission %d 체크리스트가 수정되었습니다.",
            "“%s”"
    ),

    WEEK_MISSION_TASK_ITEM_REORDERED(
            "위크미션 Mission %d 체크리스트 순서가 변경되었습니다.",
            "“%s”"
    ),

    // 멤버형 프로세스 생성/수정
    PROCESS_CREATED(
            "%s 프로세스가 생성되었습니다.",
            null
    ),
    PROCESS_UPDATED(
            "%s 프로세스가 수정되었습니다.",
            null
    ),

    // 피드백 생성
    PROCESS_FEEDBACK_CREATED(
            "%s에서 피드백이 생성되었습니다.",
            "“%s”"
    ),

    PROCESS_FEEDBACK_UPDATED(
            "%s에서 피드백이 수정되었습니다.",
                    "“%s”"
    ),

    PROCESS_FEEDBACK_DELETED(
            "%s에서 피드백이 삭제되었습니다.",
            "“%s”"
    );

    private final String mainMessageFormat;
    private final String contentMessageFormat;

    public String formatMainMessage(int maxLength, Object... args) {
        try {
            String message = String.format(mainMessageFormat, args);
            return truncate(message, maxLength);
        } catch (IllegalFormatException e) {
            throw new IllegalArgumentException("히스토리 메시지 형식이 올바르지 않습니다", e);
        }
    }

    public String formatContentMessage(int maxLength, Object... args) {
        if (!hasContent()) {
            throw new IllegalStateException("해당 히스토리는 상세 메시지를 지원하지 않습니다");
        }
        try {
            String message = String.format(contentMessageFormat, args);
            return truncate(message, maxLength);
        } catch (IllegalFormatException e) {
            throw new IllegalArgumentException("히스토리 메시지 형식이 올바르지 않습니다", e);
        }
    }

    private String truncate(String message, int maxLength) {
        if (message == null || message.length() <= maxLength) {
            return message;
        }
        throw new IllegalArgumentException("히스토리 메시지 길이가 허용 범위를 초과했습니다");
    }

    public boolean hasContent() {
        return contentMessageFormat != null && !contentMessageFormat.isBlank();
    }
}
