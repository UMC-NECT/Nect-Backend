package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class InvalidSkillCategoryException extends CustomException {
    public InvalidSkillCategoryException() {
        super(UserErrorCode.INVALID_SKILL_CATEGORY);
    }

    public InvalidSkillCategoryException(String message) {
        super(UserErrorCode.INVALID_SKILL_CATEGORY, message);
    }
}