package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class UserProfileAnalysisSaveFailed extends CustomException {
    public UserProfileAnalysisSaveFailed() {
        super(UserErrorCode.PROFILE_ANALYSIS_SAVE_FAILED);
    }

    public UserProfileAnalysisSaveFailed(String message) {
        super(UserErrorCode.PROFILE_ANALYSIS_SAVE_FAILED, message);
    }
}