package com.nect.api.domain.matching.enums;

public enum CounterParty {
    PROJECT, USER;

    public static CounterParty from(String value){
        return CounterParty.valueOf(value.toUpperCase());
    }
}
