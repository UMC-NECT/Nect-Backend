package com.nect.api.domain.user.oauth2.exception;

import com.nect.api.domain.user.oauth2.enums.OAuth2ErrorCode;
import com.nect.api.global.exception.CustomException;

public class OAuth2AuthenticationFailed extends CustomException {

    public OAuth2AuthenticationFailed() {
        super(OAuth2ErrorCode.OAUTH2_AUTHENTICATION_FAILED);
    }

    public OAuth2AuthenticationFailed(String message) {
        super(OAuth2ErrorCode.OAUTH2_AUTHENTICATION_FAILED, message);
    }
}