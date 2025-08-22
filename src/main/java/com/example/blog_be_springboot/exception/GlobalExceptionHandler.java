package com.example.blog_be_springboot.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // AppException
    @ExceptionHandler(AppException.class)
    public ProblemDetail onAppException(AppException ex) {
        var ec = ex.getErrorCode();
        return ec.toProblemDetail(ex.getMessage(), null);
    }

    // 400 - validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail onValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> {
                    assert f.getDefaultMessage() != null;
                    return Map.of("field", f.getField(), "message", f.getDefaultMessage());
                })
                .toList();
        return ErrorCode.VALIDATION.toProblemDetail("Dữ liệu không hợp lệ.", Map.of("errors", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail onBadJson(HttpMessageNotReadableException ex) {
        return ErrorCode.BAD_JSON.toProblemDetail();
    }

    // 401/403
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail onBadCredentials() {
        return ErrorCode.BAD_CREDENTIALS.toProblemDetail();
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail onForbidden() {
        return ErrorCode.FORBIDDEN.toProblemDetail();
    }

    // 404 — chung (hoặc bạn ném AppException với POST_NOT_FOUND … để cụ thể hơn)
    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail onNotFound(EntityNotFoundException ex) {
        return ErrorCode.NOT_FOUND.toProblemDetail(ex.getMessage(), null);
    }

    // 409 — xung đột (đoán nhanh một số unique key hay gặp)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail onConflict(DataIntegrityViolationException ex) {
        ex.getMostSpecificCause();
        String msg = ex.getMostSpecificCause().getMessage();
        if (msg.contains("users") && msg.contains("email")) {
            return ErrorCode.EMAIL_ALREADY_USED.toProblemDetail();
        }
        if (msg.contains("posts") && msg.contains("slug")) {
            return ErrorCode.POST_SLUG_EXISTS.toProblemDetail();
        }
        return ErrorCode.CONFLICT.toProblemDetail();
    }

    // 405/415
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail onMethodNotAllowed() {
        return ErrorCode.METHOD_NOT_ALLOWED.toProblemDetail();
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail onUnsupportedType() {
        return ErrorCode.UNSUPPORTED_MEDIA_TYPE.toProblemDetail();
    }

    // 409 - trạng thái không hợp lệ (bạn có thể ném IllegalStateException)
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail onInvalidState(IllegalStateException ex) {
        return ErrorCode.INVALID_STATE.toProblemDetail(ex.getMessage(), null);
    }

    // 500 - fallback
    @ExceptionHandler(Exception.class)
    public ProblemDetail onUnknown(Exception ex) {
        return ErrorCode.INTERNAL_ERROR.toProblemDetail();
    }
}
