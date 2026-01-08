package com.nect.api.global.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.*;
import org.zalando.logbook.core.DefaultSink;
import java.util.List;
import java.util.function.Predicate;

@Configuration
public class LogbookConfig {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_DataDog_HEADER = "x-datadog-trace-id";
    private static final String MDC_TRACE_ID = "traceId";

    @Bean
    public Logbook jsonLogbook() {
        Predicate<HttpRequest> customCondition = request -> {
            String path = request.getPath();
            return !(
                    "OPTIONS".equals(request.getMethod()) ||
                            path.contains("/health") ||
                            path.contains("/health_check") ||
                            path.contains("/actuator") ||
                            path.contains("/static") ||
                            path.contains("/swagger-ui") ||
                            path.contains("/swagger-resources") ||
                            path.contains("/v3/api-docs") ||
                            path.contains("/v2/api-docs") ||
                            path.contains("/webjars") ||
                            path.endsWith(".js") ||
                            path.endsWith(".css") ||
                            path.endsWith(".png") ||
                            path.endsWith(".ico") ||
                            path.endsWith(".html") ||
                            path.equals("/") ||
                            path.equals("/robots.txt") ||
                            path.equals("/favicon-16x16.png") ||
                            path.equals("/favicon-32x32.png") ||
                            path.equals("/swagger-ui.html") ||
                            // Bot attack and vulnerability scanning paths
                            path.contains("/.git") ||
                            path.contains("/setup.cgi") ||
                            path.contains("/docker") ||
                            path.contains("/ReportServer") ||
                            path.contains("/geoserver") ||
                            path.contains("/sitemap.xml") ||
                            path.contains("MGLNDD") ||
                            path.contains("/manager") ||
                            path.contains("/backup") ||
                            path.contains("/bin") ||
                            path.endsWith(".xml") ||
                            path.endsWith(".env")
            );
        };

        HttpLogFormatter formatter = new CustomJsonHttpLogFormatter();
        HttpLogWriter writer = new CustomHttpLogWriter();
        Sink sink = new DefaultSink(formatter, writer);

        return Logbook.builder()
                .condition(customCondition)
                .correlationId(new CustomCorrelationId())
                .sink(sink)
                .bodyFilter((contentType, body) -> maskData(body))
                .build();
    }

    private static class CustomHttpLogWriter implements HttpLogWriter {
        private static final Logger logger = LoggerFactory.getLogger("org.zalando.logbook");

        @Override
        public void write(Precorrelation precorrelation, String request) {
            logger.info(request);
        }

        @Override
        public void write(Correlation correlation, String response) {
            String level = extractLevel(response);
            switch (level) {
                case "ERROR":
                    logger.error(response);
                    break;
                case "WARN":
                    logger.warn(response);
                    break;
                default:
                    logger.info(response);
            }
        }

        private String extractLevel(String json) {
            int levelStart = json.indexOf("\"level\":\"");
            if (levelStart != -1) {
                levelStart += 9;
                int levelEnd = json.indexOf("\"", levelStart);
                if (levelEnd != -1) {
                    return json.substring(levelStart, levelEnd);
                }
            }
            return "INFO";
        }
    }

    private static class CustomCorrelationId implements CorrelationId {
        @Override
        public String generate(HttpRequest request) {
            String existingTraceId = MDC.get(MDC_TRACE_ID);
            if (existingTraceId != null && !existingTraceId.isEmpty()) {
                return existingTraceId;
            }

            String traceId;

            List<String> traceIds = request.getHeaders().get(TRACE_ID_HEADER);
            if (traceIds != null && !traceIds.isEmpty()) {
                traceId = traceIds.get(0);
            } else {
                traceIds = request.getHeaders().get(TRACE_ID_DataDog_HEADER);
                if (traceIds != null && !traceIds.isEmpty()) {
                    traceId = traceIds.get(0);
                } else {
                    traceId = java.util.UUID.randomUUID().toString();
                }
            }

            MDC.put(MDC_TRACE_ID, traceId);

            return traceId;
        }
    }

    private String maskData(String body) {
        if (body == null || body.isEmpty()) {
            return body;
        }

        body = body.replaceAll(
                "(?i)(\"[^\"]*name[^\"]*\"\\s*:\\s*\")([가-힣])[가-힣]+(\"?)",
                "$1$2**$3");

        body = body.replaceAll(
                "(?i)(\"[^\"]*phone[^\"]*\"\\s*:\\s*\")([0-9]{3})[0-9]{4}([0-9]{4})(\")",
                "$1$2****$3$4");

        return body;
    }
}