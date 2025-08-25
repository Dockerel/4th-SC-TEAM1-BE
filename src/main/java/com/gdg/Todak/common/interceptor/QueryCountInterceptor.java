package com.gdg.Todak.common.interceptor;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class QueryCountInterceptor implements HandlerInterceptor {

    public static final String UNKNOWN_PATH = "UNKNOWN_PATH";

    private final MeterRegistry meterRegistry;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String httpMethod = request.getMethod();

        String bestMatchPath = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (bestMatchPath == null) {
            bestMatchPath = UNKNOWN_PATH;
        }

        RequestContext ctx = RequestContext.builder()
                .httpMethod(httpMethod)
                .bestMatchPath(bestMatchPath)
                .build();

        RequestContextHolder.initContext(ctx);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        RequestContext ctx = RequestContextHolder.getContext();
        if (ctx != null) {
            Map<QueryType, Integer> queryCountByType = ctx.getQueryCountByType();
            queryCountByType.forEach((queryType, count) -> increment(ctx, queryType, count));
        }
    }

    private void increment(RequestContext ctx, QueryType queryType, Integer count) {
        DistributionSummary summary = DistributionSummary.builder("app.query.per_request")
                .description("Number of SQL queries per request")
                .tag("path", ctx.getBestMatchPath())
                .tag("http_method", ctx.getHttpMethod())
                .tag("query_type", queryType.name())
                .publishPercentiles(0.5, 0.95)
                .register(meterRegistry);

        summary.record(count);
    }
}
