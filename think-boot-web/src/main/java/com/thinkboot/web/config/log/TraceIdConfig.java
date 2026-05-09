package com.thinkboot.web.config.log;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "think-boot.trace-id", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TraceIdConfig {

    @Bean
    public FilterRegistrationBean<Filter> traceIdFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter());
        registration.addUrlPatterns("/*");
        registration.setName("traceIdFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    public static class TraceIdFilter implements Filter {

        private static final String TRACE_ID_KEY = "traceId";

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            String traceId = httpRequest.getHeader("X-Trace-Id");
            if (traceId == null || traceId.isEmpty()) {
                traceId = UUID.randomUUID().toString().replace("-", "");
            }

            MDC.put(TRACE_ID_KEY, traceId);
            httpResponse.setHeader("X-Trace-Id", traceId);

            try {
                chain.doFilter(request, response);
            } finally {
                MDC.remove(TRACE_ID_KEY);
            }
        }
    }
}