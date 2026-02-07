package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class UserProfileAnalysisNotFound extends CustomException {
    public UserProfileAnalysisNotFound() {
        super(UserErrorCode.PROFILE_ANALYSIS_NOT_FOUND);
    }

    public UserProfileAnalysisNotFound(String message) {
        super(UserErrorCode.PROFILE_ANALYSIS_NOT_FOUND, message);
    }
}