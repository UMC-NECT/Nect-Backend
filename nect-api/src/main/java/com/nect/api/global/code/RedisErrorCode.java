package com.nect.api.global.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RedisErrorCode implements ResponseCode {

    REDIS_PUBLISH_FAILED("RDS_301", "Redis 메시지 발행에 실패했습니다"),
    REDIS_SUBSCRIBE_FAILED("RDS_302", "Redis 메시지 수신에 실패했습니다");

    private final String statusCode;
    private final String message;
}
