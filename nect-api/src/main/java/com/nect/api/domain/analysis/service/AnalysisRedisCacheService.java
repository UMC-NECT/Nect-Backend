package com.nect.api.domain.analysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nect.api.domain.analysis.dto.res.IdeaAnalysisResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 아이디어 분석 결과를 Redis에 캐싱하는 서비스입니다.
 *
 * DB 저장이 비동기로 진행되는 동안,
 * 분석 결과를 임시로 보관/조회하는 용도로 사용합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnalysisRedisCacheService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 분석 결과를 Redis에 저장합니다.
     *
     * 직렬화 실패가 있어도 흐름을 막지 않도록 예외는 로그만 남깁니다.
     */
    public void cacheResponse(Long analysisId, IdeaAnalysisResponseDto response) {
        try {
            redisTemplate.opsForValue().set(payloadKey(analysisId), response, CACHE_TTL);
            log.info("analysis async persist done analysisId={}", analysisId);
        } catch (Exception e) {
            log.warn("analysis cache serialization failed id={}", analysisId, e);
        }
    }

    /**
     * Redis에서 분석 결과를 조회합니다.
     *
     * - DTO로 그대로 역직렬화된 경우: 캐스팅
     * - Map으로 역직렬화된 경우: ObjectMapper로 변환
     */
    public IdeaAnalysisResponseDto getCachedResponse(Long analysisId) {
        Object payload = redisTemplate.opsForValue().get(payloadKey(analysisId));
        if (payload instanceof IdeaAnalysisResponseDto) {
            return (IdeaAnalysisResponseDto) payload;
        }
        if (payload != null) {
            try {
                return objectMapper.convertValue(payload, IdeaAnalysisResponseDto.class);
            } catch (IllegalArgumentException e) {
                log.warn("analysis cache convert failed id={} type={}", analysisId, payload.getClass().getName(), e);
            }
        }
        return null;
    }

    public void evict(Long analysisId) {
        redisTemplate.delete(payloadKey(analysisId));
    }

    private String payloadKey(Long analysisId) {
        return "analysis:" + analysisId + ":payload";
    }
}
