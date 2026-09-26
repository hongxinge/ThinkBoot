package com.thinkboot.security.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private static final Pattern[] XSS_PATTERNS = new Pattern[] {
        Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        // \b 限定仅匹配词首 on 开头的事件属性（如 onerror=），避免误伤 connection= 等普通文本
        Pattern.compile("\\bon\\w+\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("data:\\s*[^,]*;base64", Pattern.CASE_INSENSITIVE)
    };

    /** JSON 请求体最大清洗大小（4MB），超过则跳过缓存清洗，避免大请求体全量驻留内存 */
    private static final int MAX_BODY_SIZE = 4 * 1024 * 1024;

    private final byte[] cachedBody;
    private final boolean isJsonRequest;

    public XssHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        String contentType = request.getContentType();
        this.isJsonRequest = contentType != null && contentType.toLowerCase().contains("application/json");
        // 仅对 JSON 请求体做读取与清洗：
        // 1. multipart/表单等请求体由后续组件（如 MultipartResolver）按流处理，
        //    提前全量缓存会导致大文件上传时内存溢出
        // 2. 表单参数已通过 getParameter* 覆写清洗，无需处理其原始请求体
        this.cachedBody = isJsonRequest ? readBody(request) : null;
    }

    private byte[] readBody(HttpServletRequest request) throws IOException {
        try (InputStream is = request.getInputStream()) {
            // 限制读取大小，超过上限的请求体不缓存清洗，直接透传原始流
            byte[] bytes = is.readNBytes(MAX_BODY_SIZE + 1);
            if (bytes.length > MAX_BODY_SIZE) {
                return null;
            }
            String body = bytes.length > 0 ? new String(bytes, StandardCharsets.UTF_8) : "";
            return xssClean(body, true).getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return value != null ? xssClean(value) : null;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        String[] cleanedValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            cleanedValues[i] = xssClean(values[i]);
        }
        return cleanedValues;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> paramMap = super.getParameterMap();
        Map<String, String[]> cleanedMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            String[] values = entry.getValue();
            String[] cleanedValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                cleanedValues[i] = xssClean(values[i]);
            }
            cleanedMap.put(entry.getKey(), cleanedValues);
        }
        return cleanedMap;
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return value != null ? xssClean(value) : null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (cachedBody == null) {
            // 非清洗范围（multipart / 超限请求体）直接透传原始流
            return super.getInputStream();
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(cachedBody);
        return new ServletInputStream() {
            @Override
            public int read() {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return bais.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (cachedBody == null) {
            return super.getReader();
        }
        return new BufferedReader(new StringReader(new String(cachedBody, StandardCharsets.UTF_8)));
    }

    @Override
    public int getContentLength() {
        return cachedBody != null ? cachedBody.length : super.getContentLength();
    }

    @Override
    public long getContentLengthLong() {
        return cachedBody != null ? cachedBody.length : super.getContentLengthLong();
    }

    private String xssClean(String value, boolean isJsonBody) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        boolean changed = true;
        while (changed) {
            String before = value;
            for (Pattern pattern : XSS_PATTERNS) {
                Matcher matcher = pattern.matcher(value);
                if (matcher.find()) {
                    value = matcher.replaceAll("");
                }
            }
            changed = !value.equals(before);
        }

        if (isJsonBody) {
            String cleaned = value;
            for (Pattern pattern : XSS_PATTERNS) {
                if (pattern.matcher(cleaned).find()) {
                    cleaned = cleaned.replace("<", "&lt;");
                    cleaned = cleaned.replace(">", "&gt;");
                    break;
                }
            }
            return cleaned;
        } else {
            // 仅转义 HTML 结构相关的核心字符集（& < > " '），
            // 不转义 / ( ) 等普通文本常用字符，避免破坏日期、算式等正常业务数据
            value = value.replace("&", "&amp;");
            value = value.replace("<", "&lt;");
            value = value.replace(">", "&gt;");
            value = value.replace("\"", "&quot;");
            value = value.replace("'", "&#x27;");
        }
        return value;
    }

    private String xssClean(String value) {
        return xssClean(value, isJsonRequest);
    }
}
