package com.nect.api.domain.user.oauth2.exception;

import com.nect.api.domain.user.oauth2.enums.OAuth2ErrorCode;
import com.nect.api.global.exception.CustomException;

public class OAuth2ProviderNotFound extends CustomException {

    public OAuth2ProviderNotFound() {
        super(OAuth2ErrorCode.OAUTH2_PROVIDER_NOT_FOUND);
    }

    public OAuth2ProviderNotFound(String message) {
        super(OAuth2ErrorCode.OAUTH2_PROVIDER_NOT_FOUND, message);
    }
}