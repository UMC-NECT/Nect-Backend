package com.nect.api.domain.team.project.exception;

import com.nect.api.global.code.ResponseCode;
import com.nect.api.global.exception.CustomException;

public class ProjectException extends CustomException {

    public ProjectException(ResponseCode code) {
        super(code);
    }
}
