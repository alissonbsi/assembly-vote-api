package br.com.sicred.assemblyvote.api.handler;

import br.com.sicred.assemblyvote.exception.AbstractException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@ControllerAdvice
@RequiredArgsConstructor
public class CustomControllerAdvice {

    private static final String INVALID_PAYLOAD_CODE = "BAD_REQUEST";

    @Value("${spring.application.name}")
    private String application;

    @ExceptionHandler(AbstractException.class)
    public ResponseEntity<CustomErrorResponse> handleBusinessException(
        HttpServletRequest request,
        AbstractException ex
    ) {
        final HttpStatus status = resolveStatus(ex);

        return ResponseEntity
            .status(status)
            .body(buildBaseResponse(request, status)
                .message(ex.getMessage())
                .code(ex.getCode())
                .build()
            );
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<CustomErrorResponse> handleValidationException(
        HttpServletRequest request,
        Exception ex
    ) {
        final List<ErrorDetail> errors = extractErrors(ex, request.getLocale());

        return ResponseEntity
            .badRequest()
            .body(buildBaseResponse(request, HttpStatus.BAD_REQUEST)
                .code(INVALID_PAYLOAD_CODE)
                .errors(errors)
                .build()
            );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorResponse> handleGenericException(
        HttpServletRequest request,
        Exception ex
    ) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(buildBaseResponse(request, HttpStatus.INTERNAL_SERVER_ERROR)
                .message("Unexpected error")
                .build()
            );
    }

    private HttpStatus resolveStatus(AbstractException ex) {
        ResponseStatus annotation = ex.getClass().getAnnotation(ResponseStatus.class);
        return annotation != null ? annotation.value() : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private List<ErrorDetail> extractErrors(Exception ex, Locale locale) {
        if (ex instanceof MethodArgumentNotValidException manv) {
            return manv.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> buildErrorDetail(error.getField(), error.getDefaultMessage(), locale))
                .toList();
        }

        if (ex instanceof BindException bindEx) {
            return bindEx.getFieldErrors()
                .stream()
                .map(error -> buildErrorDetail(error.getField(), error.getDefaultMessage(), locale))
                .toList();
        }

        return List.of();
    }

    private ErrorDetail buildErrorDetail(String field, String messageKey, Locale locale) {
        return ErrorDetail.builder()
            .field(field)
            .message(messageKey)
            .build();
    }

    private CustomErrorResponse.CustomErrorResponseBuilder buildBaseResponse(
        HttpServletRequest request,
        HttpStatus status
    ) {
        return CustomErrorResponse.builder()
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .origin(application)
            .status(status.value())
            .trace(getTraceId())
            .span(getSpanId());
    }

    private String getSpanId() {
        return MDC.get("spanId");
    }

    private String getTraceId() {
        return MDC.get("traceId");
    }
}