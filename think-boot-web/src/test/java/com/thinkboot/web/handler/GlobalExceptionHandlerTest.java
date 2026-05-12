package com.thinkboot.web.handler;

import com.thinkboot.core.exception.BusinessException;
import com.thinkboot.web.result.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleBusinessException should return R with business error code and message")
    void handleBusinessException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        BusinessException ex = new BusinessException(4001, "Business rule violation");

        R<Void> result = handler.handleBusinessException(ex, request);

        assertNotNull(result);
        assertEquals(4001, result.getCode());
        assertEquals("Business rule violation", result.getMsg());
    }

    @Test
    @DisplayName("handleBusinessException with single-arg constructor should use default code 500")
    void handleBusinessExceptionDefaultCode() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        BusinessException ex = new BusinessException("Some business error");

        R<Void> result = handler.handleBusinessException(ex, request);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("Some business error", result.getMsg());
    }

    @Test
    @DisplayName("handleValidationException should combine field error messages")
    void handleValidationException() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "username", "username cannot be empty"));
        bindingResult.addError(new FieldError("target", "email", "email format is invalid"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        R<Void> result = handler.handleValidationException(ex);

        assertNotNull(result);
        assertEquals(400, result.getCode());
        assertNotNull(result.getMsg());
        assertTrue(result.getMsg().contains("username cannot be empty"));
        assertTrue(result.getMsg().contains("email format is invalid"));
    }

    @Test
    @DisplayName("handleValidationException with single error should return single message")
    void handleValidationExceptionSingleError() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "name", "name is required"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        R<Void> result = handler.handleValidationException(ex);

        assertNotNull(result);
        assertEquals(400, result.getCode());
        assertEquals("name is required", result.getMsg());
    }

    @Test
    @DisplayName("handleConstraintViolation should extract violation messages")
    void handleConstraintViolation() {
        ConstraintViolation<String> violation = createMockViolation("must not be blank");

        Set<ConstraintViolation<String>> violations = new HashSet<>();
        violations.add(violation);

        ConstraintViolationException ex = new ConstraintViolationException(violations);

        R<Void> result = handler.handleConstraintViolation(ex);

        assertNotNull(result);
        assertEquals(400, result.getCode());
        assertEquals("must not be blank", result.getMsg());
    }

    @Test
    @DisplayName("handleConstraintViolation with multiple violations should join messages")
    void handleConstraintViolationMultiple() {
        Set<ConstraintViolation<String>> violations = new HashSet<>();
        violations.add(createMockViolation("must not be null"));
        violations.add(createMockViolation("size must be between 1 and 100"));

        ConstraintViolationException ex = new ConstraintViolationException(violations);

        R<Void> result = handler.handleConstraintViolation(ex);

        assertNotNull(result);
        assertEquals(400, result.getCode());
        assertNotNull(result.getMsg());
        assertTrue(result.getMsg().contains("must not be null"));
        assertTrue(result.getMsg().contains("size must be between 1 and 100"));
    }

    @Test
    @DisplayName("handleMissingParam should return message with parameter name")
    void handleMissingParam() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("userId", "Long");

        R<Void> result = handler.handleMissingParam(ex);

        assertNotNull(result);
        assertEquals(400, result.getCode());
        assertEquals("缺少参数: userId", result.getMsg());
    }

    @Test
    @DisplayName("handleMethodNotSupported should return 405 with method name")
    void handleMethodNotSupported() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("DELETE");

        R<Void> result = handler.handleMethodNotSupported(ex);

        assertNotNull(result);
        assertEquals(405, result.getCode());
        assertEquals("不支持的请求方法: DELETE", result.getMsg());
    }

    @Test
    @DisplayName("handleNotFound should return 404 with Chinese message")
    void handleNotFound() {
        NoResourceFoundException ex = new NoResourceFoundException(
            org.springframework.http.HttpMethod.GET, "/api/nonexistent");

        R<Void> result = handler.handleNotFound(ex);

        assertNotNull(result);
        assertEquals(404, result.getCode());
        assertEquals("接口不存在", result.getMsg());
    }

    @Test
    @DisplayName("handleException should return 500 with generic message")
    void handleException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        Exception ex = new RuntimeException("Unexpected runtime error");

        R<Void> result = handler.handleException(ex, request);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("系统异常，请稍后重试", result.getMsg());
    }

    @Test
    @DisplayName("handleException with NullPointerException should return 500")
    void handleExceptionNPE() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/crash");
        Exception ex = new NullPointerException();

        R<Void> result = handler.handleException(ex, request);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("系统异常，请稍后重试", result.getMsg());
    }

    @Test
    @DisplayName("handleException with IllegalArgumentException should return 500")
    void handleExceptionIAE() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/invalid");
        Exception ex = new IllegalArgumentException("Invalid argument");

        R<Void> result = handler.handleException(ex, request);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("系统异常，请稍后重试", result.getMsg());
    }

    private <T> ConstraintViolation<T> createMockViolation(String message) {
        return new ConstraintViolation<T>() {
            @Override public String getMessage() { return message; }
            @Override public String getMessageTemplate() { return null; }
            @Override public T getRootBean() { return null; }
            @Override public Class<T> getRootBeanClass() { return null; }
            @Override public Object getLeafBean() { return null; }
            @Override public Object[] getExecutableParameters() { return null; }
            @Override public Object getExecutableReturnValue() { return null; }
            @Override public Path getPropertyPath() { return null; }
            @Override public Object getInvalidValue() { return null; }
            @Override public ConstraintDescriptor<?> getConstraintDescriptor() { return null; }
            @Override public <U> U unwrap(Class<U> type) { return null; }
        };
    }
}
