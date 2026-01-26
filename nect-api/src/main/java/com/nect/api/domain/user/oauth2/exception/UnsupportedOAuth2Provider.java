package com.nect.api.domain.user.oauth2.exception;

import com.nect.api.domain.user.oauth2.enums.OAuth2ErrorCode;
import com.nect.api.global.exception.CustomException;

public class UnsupportedOAuth2Provider extends CustomException {

    public UnsupportedOAuth2Provider() {
        super(OAuth2ErrorCode.UNSUPPORTED_OAUTH2_PROVIDER);
    }

    public UnsupportedOAuth2Provider(String message) {
        super(OAuth2ErrorCode.UNSUPPORTED_OAUTH2_PROVIDER, message);
    }
}