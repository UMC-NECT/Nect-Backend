package com.nect.api.domain.user.exception;

import com.nect.api.domain.user.enums.UserErrorCode;
import com.nect.api.global.exception.CustomException;

public class UserProfileAnalysisParsingFailed extends CustomException {
    public UserProfileAnalysisParsingFailed() {
        super(UserErrorCode.PROFILE_ANALYSIS_PARSING_FAILED);
    }

    public UserProfileAnalysisParsingFailed(String message) {
        super(UserErrorCode.PROFILE_ANALYSIS_PARSING_FAILED, message);
    }
}