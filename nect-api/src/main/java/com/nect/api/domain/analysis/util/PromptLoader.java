package com.nect.api.domain.analysis.util;


import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;


@Component
public class PromptLoader {


    public String loadPrompt(String promptPath, Map<String, String> variables) {
        try {
            // 프롬프트 파일 읽기
            ClassPathResource resource = new ClassPathResource(promptPath);
            String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            // 변수 치환
            String result = template;
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? entry.getValue() : "";
                result = result.replace(placeholder, value);
            }

            return result;

        } catch (IOException e) {
            throw new RuntimeException("프롬프트 파일 로드 실패: " + promptPath, e);
        }
    }
}