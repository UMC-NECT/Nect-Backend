package com.nect.core.entity.team.workspace.enums;

public enum ActionType {
    // 프로세스(TASK)
    PROCESS_CREATE, PROCESS_UPDATE, PROCESS_MOVE_STATUS, PROCESS_ASSIGN,
    // 문서
    DOCUMENT_UPLOAD, DOCUMENT_DELETE,
    // 게시글
    POST_CREATE, COMMENT_CREATE, POST_DELETE,
    // 멤버
    MEMBER_INVITE, MEMBER_KICK
}
