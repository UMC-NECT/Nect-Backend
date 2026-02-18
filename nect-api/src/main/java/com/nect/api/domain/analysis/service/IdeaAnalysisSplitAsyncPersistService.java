package com.nect.api.domain.analysis.service;

import com.nect.api.domain.analysis.dto.res.IdeaAnalysisResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 분할 분석 결과를 비동기로 DB에 저장하는 서비스입니다.
 *
 * Redis 캐시에 임시 저장된 분석 결과를 읽어와
 * IdeaAnalysisBulkRepository를 통해 일괄 저장합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IdeaAnalysisSplitAsyncPersistService {

    private final AnalysisRedisCacheService analysisRedisCacheService;
    private final IdeaAnalysisBulkPersistService ideaAnalysisBulkPersistService;

    /**
     * Redis 캐시에서 분석 결과를 읽어와 비동기로 저장합니다.
     */
    @Async("analysisAsyncSaveExecutor")
    @Transactional
    public void persistFromCacheAsync(Long analysisId) {
        IdeaAnalysisResponseDto cached = analysisRedisCacheService.getCachedResponse(analysisId);
        if (cached == null) {
            return;
        }
        try {
            ideaAnalysisBulkPersistService.updateAnalysisWithDetails(analysisId, cached);
        } catch (Exception e) {
            log.warn("analysis async persist failed id={}", analysisId, e);
        }
    }
}
