package com.thinkboot.security.filter;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;

class XssFilterTest {

    private final XssFilter xssFilter = new XssFilter();

    @Test
    @DisplayName("XSS script tag should be sanitized")
    void xssScriptTagSanitization() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json");
        request.setMethod("POST");
        request.setRequestURI("/api/test");
        request.setContent("{\"input\": \"<script>alert('xss')</script>\"}".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        PrintWriter writer = response.getWriter();

        MockFilterChain chain = new MockFilterChain();
        xssFilter.doFilter(request, response, chain);

        XssHttpServletRequestWrapper wrapper = (XssHttpServletRequestWrapper) chain.getCapturedRequest();
        String body = wrapper.getReader().readLine();

        assertNotNull(body);
        assertFalse(body.contains("<script>"));
        assertFalse(body.contains("</script>"));
        assertFalse(body.contains("alert"));
    }

    @Test
    @DisplayName("javascript: URI should be sanitized")
    void xssJavascriptUriSanitization() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json");
        request.setMethod("POST");
        request.setRequestURI("/api/test");
        request.setContent("{\"url\": \"javascript:void(0)\"}".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();
        xssFilter.doFilter(request, response, chain);

        XssHttpServletRequestWrapper wrapper = (XssHttpServletRequestWrapper) chain.getCapturedRequest();
        String body = wrapper.getReader().readLine();

        assertNotNull(body);
        assertFalse(body.toLowerCase().contains("javascript:"));
    }

    @Test
    @DisplayName("on* event handlers should be sanitized")
    void xssEventHandlerSanitization() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json");
        request.setMethod("POST");
        request.setRequestURI("/api/test");
        request.setContent("{\"html\": \"<img src=x onerror=alert(1)>\"}".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();
        xssFilter.doFilter(request, response, chain);

        XssHttpServletRequestWrapper wrapper = (XssHttpServletRequestWrapper) chain.getCapturedRequest();
        String body = wrapper.getReader().readLine();

        assertNotNull(body);
        assertFalse(body.toLowerCase().contains("onerror"));
        assertFalse(body.toLowerCase().contains("onclick"));
    }

    @Test
    @DisplayName("eval() should be removed")
    void xssEvalRemoval() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json");
        request.setMethod("POST");
        request.setRequestURI("/api/test");
        request.setContent("{\"code\": \"eval('malicious')\"}".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();
        xssFilter.doFilter(request, response, chain);

        XssHttpServletRequestWrapper wrapper = (XssHttpServletRequestWrapper) chain.getCapturedRequest();
        String body = wrapper.getReader().readLine();

        assertNotNull(body);
        assertFalse(body.toLowerCase().contains("eval("));
    }

    @Test
    @DisplayName("expression() should be removed")
    void xssExpressionRemoval() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json");
        request.setMethod("POST");
        request.setRequestURI("/api/test");
        request.setContent("{\"style\": \"expression(alert('xss'))\"}".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();
        xssFilter.doFilter(request, response, chain);

        XssHttpServletRequestWrapper wrapper = (XssHttpServletRequestWrapper) chain.getCapturedRequest();
        String body = wrapper.getReader().readLine();

        assertNotNull(body);
        assertFalse(body.toLowerCase().contains("expression("));
    }

    @Test
    @DisplayName("HTML special characters should be encoded")
    void xssHtmlEncoding() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("text/plain");
        request.setMethod("GET");
        request.setRequestURI("/api/test");
        request.setParameter("input", "<test>value('with')\"quotes\"");

        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();
        xssFilter.doFilter(request, response, chain);

        XssHttpServletRequestWrapper wrapper = (XssHttpServletRequestWrapper) chain.getCapturedRequest();
        String value = wrapper.getParameter("input");

        assertNotNull(value);
        assertTrue(value.contains("&lt;"), "Should encode < as &lt;");
        assertTrue(value.contains("&gt;"), "Should encode > as &gt;");
        assertTrue(value.contains("&#40;"), "Should encode ( as &#40;");
        assertTrue(value.contains("&#41;"), "Should encode ) as &#41;");
        assertTrue(value.contains("&quot;"), "Should encode \" as &quot;");
        assertTrue(value.contains("&#x27;"), "Should encode ' as &#x27;");
    }

    @Test
    @DisplayName("Valid JSON data should not be corrupted")
    void validJsonNotCorrupted() throws Exception {
        String validJson = "{\"name\": \"John\", \"age\": 30, \"email\": \"john@example.com\"}";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json");
        request.setMethod("POST");
        request.setRequestURI("/api/test");
        request.setContent(validJson.getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();
        xssFilter.doFilter(request, response, chain);

        XssHttpServletRequestWrapper wrapper = (XssHttpServletRequestWrapper) chain.getCapturedRequest();
        BufferedReader reader = wrapper.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String body = sb.toString();

        assertNotNull(body);
        assertTrue(body.contains("\"name\""));
        assertTrue(body.contains("John"));
        assertTrue(body.contains("\"age\""));
        assertTrue(body.contains("30"));
        assertTrue(body.contains("john@example.com"));
    }

    @Test
    @DisplayName("Swagger paths should be excluded from XSS filtering")
    void swaggerPathExcluded() throws Exception {
        String[] swaggerPaths = {
                "/swagger-ui/index.html",
                "/v3/api-docs/openapi",
                "/doc.html",
                "/webjars/swagger-ui/swagger-ui-bundle.js"
        };

        for (String path : swaggerPaths) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setContentType("application/json");
            request.setMethod("POST");
            request.setRequestURI(path);
            request.setContent("{\"input\": \"<script>alert('xss')</script>\"}".getBytes());

            MockHttpServletResponse response = new MockHttpServletResponse();

            MockFilterChain chain = new MockFilterChain();
            xssFilter.doFilter(request, response, chain);

            assertSame(request, chain.getCapturedRequest(),
                    "Request to " + path + " should not be wrapped");
        }
    }

    @Test
    @DisplayName("Non-swagger paths should be filtered")
    void nonSwaggerPathFiltered() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json");
        request.setMethod("POST");
        request.setRequestURI("/api/users");
        request.setContent("{\"name\": \"<script>alert('xss')</script>\"}".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();
        xssFilter.doFilter(request, response, chain);

        assertNotSame(request, chain.getCapturedRequest(),
                "Request to /api/users should be wrapped for XSS filtering");
    }

    @Test
    @DisplayName("Empty request body should be handled correctly")
    void emptyRequestBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json");
        request.setMethod("POST");
        request.setRequestURI("/api/test");
        request.setContent("".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();
        xssFilter.doFilter(request, response, chain);

        XssHttpServletRequestWrapper wrapper = (XssHttpServletRequestWrapper) chain.getCapturedRequest();
        assertNotNull(wrapper);
        assertEquals(0, wrapper.getContentLength());
    }

    @Test
    @DisplayName("vbscript: URI should be sanitized")
    void xssVbscriptUriSanitization() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json");
        request.setMethod("POST");
        request.setRequestURI("/api/test");
        request.setContent("{\"url\": \"vbscript:msgbox('xss')\"}".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();
        xssFilter.doFilter(request, response, chain);

        XssHttpServletRequestWrapper wrapper = (XssHttpServletRequestWrapper) chain.getCapturedRequest();
        String body = wrapper.getReader().readLine();

        assertNotNull(body);
        assertFalse(body.toLowerCase().contains("vbscript:"));
    }

    @Test
    @DisplayName("base64 data URI should be sanitized")
    void xssBase64DataUriSanitization() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json");
        request.setMethod("POST");
        request.setRequestURI("/api/test");
        request.setContent("{\"src\": \"data:image/png;base64,abc123\"}".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();
        xssFilter.doFilter(request, response, chain);

        XssHttpServletRequestWrapper wrapper = (XssHttpServletRequestWrapper) chain.getCapturedRequest();
        String body = wrapper.getReader().readLine();

        assertNotNull(body);
        assertFalse(body.toLowerCase().contains("data:"));
    }

    private static class MockFilterChain implements jakarta.servlet.FilterChain {
        private jakarta.servlet.ServletRequest capturedRequest;
        private jakarta.servlet.ServletResponse capturedResponse;

        @Override
        public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) throws IOException, ServletException {
            this.capturedRequest = request;
            this.capturedResponse = response;
        }

        public jakarta.servlet.ServletRequest getCapturedRequest() {
            return capturedRequest;
        }

        public jakarta.servlet.ServletResponse getCapturedResponse() {
            return capturedResponse;
        }
    }
}
