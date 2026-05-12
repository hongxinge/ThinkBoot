package com.thinkboot.web.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServletUtilsTest {

    @Test
    @DisplayName("getClientIp should return x-forwarded-for when present")
    void getClientIpWithXForwardedFor() {
        HttpServletRequest request = createMockRequest("192.168.1.1", "10.0.0.1", "172.16.0.1", null);

        String ip = ServletUtils.getClientIp(request);

        assertEquals("192.168.1.1", ip);
    }

    @Test
    @DisplayName("getClientIp should return first IP when x-forwarded-for has multiple IPs")
    void getClientIpWithMultipleXForwardedFor() {
        HttpServletRequest request = createMockRequest("192.168.1.1, 10.0.0.1, 172.16.0.1", null, null, null);

        String ip = ServletUtils.getClientIp(request);

        assertEquals("192.168.1.1", ip);
    }

    @Test
    @DisplayName("getClientIp should trim whitespace from first IP")
    void getClientIpTrimsWhitespace() {
        HttpServletRequest request = createMockRequest("  192.168.1.1  , 10.0.0.1", null, null, null);

        String ip = ServletUtils.getClientIp(request);

        assertEquals("192.168.1.1", ip);
    }

    @Test
    @DisplayName("getClientIp should fallback to Proxy-Client-IP when x-forwarded-for is blank")
    void getClientIpFallbackToProxyClientIp() {
        HttpServletRequest request = createMockRequest("", "10.0.0.1", "172.16.0.1", null);

        String ip = ServletUtils.getClientIp(request);

        assertEquals("10.0.0.1", ip);
    }

    @Test
    @DisplayName("getClientIp should fallback to Proxy-Client-IP when x-forwarded-for is unknown")
    void getClientIpFallbackToProxyClientIpWhenUnknown() {
        HttpServletRequest request = createMockRequest("unknown", "10.0.0.1", null, null);

        String ip = ServletUtils.getClientIp(request);

        assertEquals("10.0.0.1", ip);
    }

    @Test
    @DisplayName("getClientIp should fallback to X-Real-IP when previous headers are blank")
    void getClientIpFallbackToXRealIp() {
        HttpServletRequest request = createMockRequest("", "", "172.16.0.1", "127.0.0.1");

        String ip = ServletUtils.getClientIp(request);

        assertEquals("172.16.0.1", ip);
    }

    @Test
    @DisplayName("getClientIp should fallback to X-Real-IP when previous headers are unknown")
    void getClientIpFallbackToXRealIpWhenUnknown() {
        HttpServletRequest request = createMockRequest("unknown", "unknown", "172.16.0.1", null);

        String ip = ServletUtils.getClientIp(request);

        assertEquals("172.16.0.1", ip);
    }

    @Test
    @DisplayName("getClientIp should fallback to getRemoteAddr when all headers are blank")
    void getClientIpFallbackToRemoteAddr() {
        HttpServletRequest request = createMockRequest("", "", "", "127.0.0.1");

        String ip = ServletUtils.getClientIp(request);

        assertEquals("127.0.0.1", ip);
    }

    @Test
    @DisplayName("getClientIp should fallback to getRemoteAddr when all headers are null")
    void getClientIpFallbackToRemoteAddrWhenNull() {
        HttpServletRequest request = createMockRequest(null, null, null, "192.168.0.100");

        String ip = ServletUtils.getClientIp(request);

        assertEquals("192.168.0.100", ip);
    }

    @Test
    @DisplayName("getClientIp should return 'unknown' when request is null")
    void getClientIpWithNullRequest() {
        String ip = ServletUtils.getClientIp(null);

        assertEquals("unknown", ip);
    }

    @Test
    @DisplayName("getClientIp should return getRemoteAddr when all headers are unknown")
    void getClientIpAllHeadersUnknown() {
        HttpServletRequest request = createMockRequest("unknown", "unknown", "unknown", "10.10.10.10");

        String ip = ServletUtils.getClientIp(request);

        assertEquals("10.10.10.10", ip);
    }

    @Test
    @DisplayName("getClientIp should handle case-insensitive 'unknown' check")
    void getClientIpCaseInsensitiveUnknown() {
        HttpServletRequest request = createMockRequest("UNKNOWN", "Unknown", "172.16.0.1", null);

        String ip = ServletUtils.getClientIp(request);

        assertEquals("172.16.0.1", ip);
    }

    @Test
    @DisplayName("getUserAgent should return User-Agent header value")
    void getUserAgent() {
        HttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String getHeader(String name) {
                return "User-Agent".equals(name) ? "Mozilla/5.0 (Windows NT 10.0; Win64; x64)" : null;
            }
            @Override
            public String getRemoteAddr() {
                return null;
            }
        };

        String userAgent = ServletUtils.getUserAgent(request);

        assertEquals("Mozilla/5.0 (Windows NT 10.0; Win64; x64)", userAgent);
    }

    @Test
    @DisplayName("getUserAgent should return 'unknown' when request is null")
    void getUserAgentWithNullRequest() {
        String userAgent = ServletUtils.getUserAgent(null);

        assertEquals("unknown", userAgent);
    }

    @Test
    @DisplayName("getRequest should return null when outside request context")
    void getRequestOutsideContext() {
        HttpServletRequest request = ServletUtils.getRequest();

        assertNull(request);
    }

    private HttpServletRequest createMockRequest(String xForwardedFor, String proxyClientIp, String xRealIp, String remoteAddr) {
        return new MockHttpServletRequest() {
            @Override
            public String getHeader(String name) {
                if ("x-forwarded-for".equals(name)) return xForwardedFor;
                if ("Proxy-Client-IP".equals(name)) return proxyClientIp;
                if ("X-Real-IP".equals(name)) return xRealIp;
                return null;
            }

            @Override
            public String getRemoteAddr() {
                return remoteAddr;
            }
        };
    }

    private abstract static class MockHttpServletRequest implements HttpServletRequest {
        @Override public String getAuthType() { return null; }
        @Override public jakarta.servlet.http.Cookie[] getCookies() { return new jakarta.servlet.http.Cookie[0]; }
        @Override public long getDateHeader(String name) { return 0; }
        @Override public java.util.Enumeration<String> getHeaders(String name) { return java.util.Collections.emptyEnumeration(); }
        @Override public java.util.Enumeration<String> getHeaderNames() { return java.util.Collections.emptyEnumeration(); }
        @Override public int getIntHeader(String name) { return 0; }
        @Override public String getMethod() { return null; }
        @Override public String getPathInfo() { return null; }
        @Override public String getPathTranslated() { return null; }
        @Override public String getContextPath() { return null; }
        @Override public String getQueryString() { return null; }
        @Override public String getRemoteUser() { return null; }
        @Override public boolean isUserInRole(String role) { return false; }
        @Override public java.security.Principal getUserPrincipal() { return null; }
        @Override public String getRequestedSessionId() { return null; }
        @Override public String getRequestURI() { return null; }
        @Override public StringBuffer getRequestURL() { return null; }
        @Override public String getServletPath() { return null; }
        @Override public jakarta.servlet.http.HttpSession getSession(boolean create) { return null; }
        @Override public jakarta.servlet.http.HttpSession getSession() { return null; }
        @Override public String changeSessionId() { return null; }
        @Override public boolean isRequestedSessionIdValid() { return false; }
        @Override public boolean isRequestedSessionIdFromCookie() { return false; }
        @Override public boolean isRequestedSessionIdFromURL() { return false; }
        @Override public boolean authenticate(jakarta.servlet.http.HttpServletResponse response) { return false; }
        @Override public void login(String username, String password) {}
        @Override public void logout() {}
        @Override public java.util.Collection<jakarta.servlet.http.Part> getParts() { return java.util.Collections.emptyList(); }
        @Override public jakarta.servlet.http.Part getPart(String name) { return null; }
        @Override public <T extends jakarta.servlet.http.HttpUpgradeHandler> T upgrade(Class<T> handlerClass) { return null; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public java.util.Enumeration<String> getAttributeNames() { return java.util.Collections.emptyEnumeration(); }
        @Override public String getCharacterEncoding() { return null; }
        @Override public void setCharacterEncoding(String env) {}
        @Override public int getContentLength() { return 0; }
        @Override public long getContentLengthLong() { return 0; }
        @Override public String getContentType() { return null; }
        @Override public jakarta.servlet.ServletInputStream getInputStream() { return null; }
        @Override public String getParameter(String name) { return null; }
        @Override public java.util.Enumeration<String> getParameterNames() { return java.util.Collections.emptyEnumeration(); }
        @Override public String[] getParameterValues(String name) { return null; }
        @Override public java.util.Map<String, String[]> getParameterMap() { return java.util.Collections.emptyMap(); }
        @Override public String getProtocol() { return null; }
        @Override public String getScheme() { return null; }
        @Override public String getServerName() { return null; }
        @Override public int getServerPort() { return 0; }
        @Override public java.io.BufferedReader getReader() { return null; }
        @Override public String getRemoteHost() { return null; }
        @Override public void setAttribute(String name, Object o) {}
        @Override public void removeAttribute(String name) {}
        @Override public java.util.Locale getLocale() { return java.util.Locale.getDefault(); }
        @Override public java.util.Enumeration<java.util.Locale> getLocales() { return java.util.Collections.emptyEnumeration(); }
        @Override public boolean isSecure() { return false; }
        @Override public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) { return null; }
        @Override public int getRemotePort() { return 0; }
        @Override public String getLocalName() { return null; }
        @Override public String getLocalAddr() { return null; }
        @Override public int getLocalPort() { return 0; }
        @Override public jakarta.servlet.ServletContext getServletContext() { return null; }
        @Override public jakarta.servlet.AsyncContext startAsync() { return null; }
        @Override public jakarta.servlet.AsyncContext startAsync(jakarta.servlet.ServletRequest servletRequest, jakarta.servlet.ServletResponse servletResponse) { return null; }
        @Override public boolean isAsyncStarted() { return false; }
        @Override public boolean isAsyncSupported() { return false; }
        @Override public jakarta.servlet.AsyncContext getAsyncContext() { return null; }
        @Override public jakarta.servlet.DispatcherType getDispatcherType() { return null; }
        @Override public String getRequestId() { return null; }
        @Override public String getProtocolRequestId() { return null; }
        @Override public jakarta.servlet.ServletConnection getServletConnection() { return null; }
    }
}
