package com.nect.api.domain.team.project.exception;

import com.nect.api.global.code.ResponseCode;
import com.nect.api.global.exception.CustomException;

public class ProjectUserException extends CustomException {

    public ProjectUserException(ResponseCode code) {
        super(code);
    }
}
