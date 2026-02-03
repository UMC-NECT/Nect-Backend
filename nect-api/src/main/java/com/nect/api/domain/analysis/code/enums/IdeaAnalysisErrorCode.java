package com.nect.api.domain.analysis.code.enums;
import com.nect.api.global.code.ResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum IdeaAnalysisErrorCode implements ResponseCode {

    TOO_MANY_ANALYSIS(HttpStatus.BAD_REQUEST, "ANALYSIS-001", "아이디어 분석은 인당 최대 2개까지만 가능합니다."),
    ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ANALYSIS-002", "AI 분석 수행 중 오류가 발생했습니다."),
    DATA_CONVERSION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ANALYSIS-003", "분석 결과 데이터 처리 중 오류가 발생했습니다.");
    private final HttpStatus httpStatus;
    private final String statusCode;
    private final String message;

    @Override
    public String getStatusCode() {
        return this.statusCode;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}