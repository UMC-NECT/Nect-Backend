package com.nect.api.domain.mypage.converter;

import com.nect.api.global.code.CommonResponseCode;
import com.nect.api.global.exception.CustomException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.List;

/**
 * Project 엔티티의 속성 값을
 * DB(String) <-> Server(List<String>) 변환을 담당합니다
 * String은 를 포함할 수 없습니다.
 */
@Component
@Converter
public class ProjectListConverter implements AttributeConverter<List<String>, String> {

    // 요소 간 구분자
    private static final String SEPARATOR = "\u001F";

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "";
        }

        return attribute.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .peek(value -> {
                    if (value.contains(SEPARATOR)) {
                        System.out.println("hello");
                        throw new CustomException(CommonResponseCode.BAD_REQUEST_ERROR, "부적절한 단어가 포함되어있습니다. 내용에 \u001F 를 포함할 수 없습니다.");
                    }
                })
                .reduce((left, right) -> left + SEPARATOR + right)
                .orElse("");
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();
        }

        return Arrays.stream(dbData.split(SEPARATOR))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

}
