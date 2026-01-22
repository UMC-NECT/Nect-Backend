package com.nect.core.entity.notifications.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.IllegalFormatException;

/**
 *
 * 알림 메시지의 포맷을 관리하는 Enum입니다.
 *
 * mainMessage : 알림의 기본 메시지입니다.
 * contentMessage : 일림에 댓글, 언급에 대한 내용이 있다면 담는 부분입니다.
 *
 */
@Getter
@AllArgsConstructor
public enum NotificationType {

    MATCHING_REQUEST_RECEIVED(
            "새로운 신청이 있습니다! 프로필을 확인해 보세요.",
            null
    ),

    MATCHING_REQUEST_CANCELED(
            "%s의 신청이 취소되었습니다.",
            null
    ),

    MATCHING_ACCEPTED(
            "축하합니다! 프로젝트/팀원 매칭이 수락되었습니다. 작업실로 이동해보세요.",
            null
    ),

    MATCHING_REJECTED(
            "아쉽게도 %s와 매칭되지 않았습니다. 다른 프로젝트/팀원을 찾아볼까요?",
            null
    ),

    CHAT_REQUEST_RECEIVED(
            "%s이 나에게 대화를 요청했습니다.",
            "“%s”"
    ),

    CHAT_MESSAGE_RECEIVED(
            "%s이 나에게 메시지를 보냈습니다.",
            "“%s”"
    ),

    MATCHING_DEADLINE_SOON(
            "관심 있는 %s의 신청 마감이 %d일 남았습니다! 놓치지 말고 지원하세요.",
            null
    ),

    WORKSPACE_FILE_UPLOADED(
            "%s님이 새로운 파일을 업로드했습니다.",
            "[파일명: %s]"
    ),

    WORKSPACE_MENTIONED(
            "%s님이 나를 언급했습니다.",
            "“%s”"
    ),

    WORKSPACE_MEMBER_JOINED(
            "새로운 팀원 %s님이 합류했습니다. 반갑게 인사해 보세요!",
            null
    ),

    WORKSPACE_MISSION_CREATED(
            "Mission%d 블록이 생성되었습니다.",
            "“%s”"
    ),

    WORKSPACE_MISSION_UPDATED(
            "Mission%d 블록이 수정되었습니다.",
            "“%s”"
    ),

    WORKSPACE_TASK_FEEDBACK(
            "%s님이 나의 작업에 피드백을 남겼습니다.",
            "“%s”"
    );

    private final String mainMessageFormat;
    private final String contentMessageFormat;

    // 문자열 포맷팅
    public String formatMainMessage(Object... args) {
        try {
            return String.format(mainMessageFormat, args);
        } catch (IllegalFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid main message args for NotificationType: " + name(),
                    e
            );
        }
    }

    public String formatContentMessage(Object... args) {
        if (!hasContent()) {
            throw new IllegalStateException(name() + " does not support content message");
        }

        try {
            return String.format(contentMessageFormat, args);
        } catch (IllegalFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid content message args for NotificationType: " + name(),
                    e
            );
        }
    }

    // contentMessageFormat이 있는지 반환.
    public boolean hasContent() {
        return contentMessageFormat != null && !contentMessageFormat.isBlank();
    }


}
