package com.thinkboot.security.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * XSS 防护过滤器
 * 自动转义请求参数中的 HTML 和 JavaScript 代码，防止 XSS 攻击
 */
public class XssFilter implements Filter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> EXCLUDE_PATHS = new ArrayList<>();

    static {
        EXCLUDE_PATHS.add("/swagger-ui/**");
        EXCLUDE_PATHS.add("/v3/api-docs/**");
        EXCLUDE_PATHS.add("/doc.html");
        EXCLUDE_PATHS.add("/webjars/**");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        boolean isExcluded = EXCLUDE_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (isExcluded) {
            chain.doFilter(request, response);
        } else {
            chain.doFilter(new XssHttpServletRequestWrapper(httpRequest), response);
        }
    }
}
