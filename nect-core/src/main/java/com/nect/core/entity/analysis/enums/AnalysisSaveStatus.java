package com.nect.core.entity.analysis.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AnalysisSaveStatus {

    PENDING("PENDING")
    ;

    private final String status;
}
