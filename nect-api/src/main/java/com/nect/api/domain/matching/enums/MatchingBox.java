package com.nect.api.domain.matching.enums;

public enum MatchingBox {
    RECEIVED, SENT;

    public static MatchingBox from(String value){
        return MatchingBox.valueOf(value.toUpperCase());
    }
}
