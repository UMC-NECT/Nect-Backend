package com.nect.api.domain.mypage.exception;

import com.nect.api.domain.mypage.enums.UserTeamRoleErrorCode;
import com.nect.api.global.exception.CustomException;

public class UserTeamRoleException extends CustomException {
    public UserTeamRoleException(UserTeamRoleErrorCode errorCode) {
        super(errorCode);
    }

    public UserTeamRoleException(UserTeamRoleErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
