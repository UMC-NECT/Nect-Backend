package com.nect.api.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 비동기 실행용 TaskExecutor를 구성합니다.
 *
 * @Async 기반의 비동기 작업이
 * 예측 가능한 스레드 풀에서 실행되도록 전용 executor를 제공합니다.
 * (기본 executor 탐색 경고를 방지하고, 실행 정책을 명확히 합니다.)
 */
@Configuration
public class AsyncConfig {

    /**
     * 분석 결과 비동기 저장 전용 executor.
     *
     * - core/max/queue를 명시해 과도한 스레드 생성 방지
     * - 스레드 이름 prefix로 로그 식별성 확보
     */
    @Bean(name = "analysisAsyncSaveExecutor")
    public TaskExecutor analysisAsyncSaveExecutor() {
        int cores = Runtime.getRuntime().availableProcessors();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Math.max(2, cores));
        executor.setMaxPoolSize(Math.max(4, cores * 2));
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("nect-async-");
        executor.initialize();
        return executor;
    }
}
