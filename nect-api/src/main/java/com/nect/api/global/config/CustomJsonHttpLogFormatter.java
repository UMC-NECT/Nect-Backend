package com.nect.api.global.config;

import org.slf4j.MDC;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.HttpResponse;
import org.zalando.logbook.json.JsonHttpLogFormatter;

import java.io.IOException;

public class CustomJsonHttpLogFormatter implements HttpLogFormatter {
    private static final String MDC_METHOD = "logbook.method";
    private static final String MDC_PATH = "logbook.path";
    private static final String MDC_QUERY = "logbook.query";
    private static final String MDC_TRACE_ID = "traceId";

    private final JsonHttpLogFormatter delegate = new JsonHttpLogFormatter();

    @Override
    public String format(Precorrelation precorrelation, HttpRequest request) throws IOException {
        try {
            String method = request.getMethod();
            String path = request.getPath();
            String query = request.getQuery();

            MDC.put(MDC_METHOD, method);
            MDC.put(MDC_PATH, path);
            MDC.put(MDC_QUERY, query);

            String traceId = MDC.get(MDC_TRACE_ID);
            if (traceId == null) {
                traceId = precorrelation.getId();
            }

            String message = String.format("[REQUEST] %s %s%s [%s]",
                    method,
                    path,
                    query.isEmpty() ? "" : "?" + query,
                    traceId);

            String json = delegate.format(precorrelation, request);
            json = normalizeJson(json);

            return String.format("{\"level\":\"INFO\",\"message\":%s,\"method\":\"%s\",\"path\":\"%s\",%s",
                    escape(message),
                    method.replace("\"", "\\\""),
                    path.replace("\"", "\\\""),
                    json.substring(1));
        } catch (Exception e) {
            clearMDC();
            throw e;
        }
    }

    @Override
    public String format(Correlation correlation, HttpResponse response) throws IOException {
        try {
            String method = MDC.get(MDC_METHOD);
            String path = MDC.get(MDC_PATH);
            String query = MDC.get(MDC_QUERY);
            String traceId = MDC.get(MDC_TRACE_ID);

            String message = String.format("[RESPONSE] %d %s%s [%s]",
                    response.getStatus(),
                    path != null ? path : "UNKNOWN",
                    (query != null && !query.isEmpty()) ? "?" + query : "",
                    traceId != null ? traceId : correlation.getId());

            String json = delegate.format(correlation, response);
            json = normalizeJson(json);

            String level;
            int status = response.getStatus();
            if (status >= 500) {
                level = "ERROR";
            } else if (status >= 400) {
                level = "WARN";
            } else {
                level = "INFO";
            }

            return String.format("{\"level\":\"%s\",\"message\":%s,\"method\":\"%s\",\"path\":\"%s\",%s",
                    level,
                    escape(message),
                    method != null ? method.replace("\"", "\\\"") : "UNKNOWN",
                    path != null ? path.replace("\"", "\\\"") : "UNKNOWN",
                    json.substring(1));
        } finally {
            MDC.clear();
        }
    }

    private void clearMDC() {
        MDC.remove(MDC_METHOD);
        MDC.remove(MDC_PATH);
        MDC.remove(MDC_QUERY);
        MDC.remove(MDC_TRACE_ID);
    }

    private String escape(String s) {
        return "\"" + s.replace("\"", "\\\"") + "\"";
    }

    private String normalizeJson(String json) {
        return json.replaceAll("[\\n\\r\\t]", "").replaceAll("\\s+", " ");
    }
}