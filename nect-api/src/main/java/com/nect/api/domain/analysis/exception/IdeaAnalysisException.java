package com.nect.api.domain.analysis.exception;

import com.nect.api.domain.analysis.code.enums.IdeaAnalysisErrorCode;
import com.nect.api.global.exception.CustomException;
import lombok.Getter;
@Getter
public class IdeaAnalysisException extends CustomException {


    public IdeaAnalysisException(IdeaAnalysisErrorCode errorCode) {
        super(errorCode);
    }

    public IdeaAnalysisException(IdeaAnalysisErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public IdeaAnalysisException(IdeaAnalysisErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message);

    }

    public IdeaAnalysisErrorCode getIdeaAnalysisErrorCode() {
        return (IdeaAnalysisErrorCode) getResponseCode();
    }
}