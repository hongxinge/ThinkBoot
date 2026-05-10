package com.thinkboot.security.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.List;

public class XssFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(XssFilter.class);

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final List<String> EXCLUDE_PATHS = List.of(
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/doc.html",
            "/webjars/**"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        boolean isExcluded = EXCLUDE_PATHS.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));

        if (isExcluded) {
            chain.doFilter(request, response);
        } else {
            XssHttpServletRequestWrapper wrappedRequest;
            try {
                wrappedRequest = new XssHttpServletRequestWrapper(httpRequest);
            } catch (IOException e) {
                log.warn("Failed to wrap request for XSS filtering on path: {}, continuing without wrapping", path, e);
                wrappedRequest = null;
            }
            chain.doFilter(wrappedRequest != null ? wrappedRequest : request, response);
        }
    }
}
