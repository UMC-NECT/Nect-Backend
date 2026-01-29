package com.nect.api.domain.user.oauth2.exception;

import com.nect.api.domain.user.oauth2.enums.OAuth2ErrorCode;
import com.nect.api.global.exception.CustomException;

public class OAuth2UserInfoInvalid extends CustomException {

    public OAuth2UserInfoInvalid() {
        super(OAuth2ErrorCode.OAUTH2_USER_INFO_INVALID);
    }

    public OAuth2UserInfoInvalid(String message) {
        super(OAuth2ErrorCode.OAUTH2_USER_INFO_INVALID, message);
    }
}