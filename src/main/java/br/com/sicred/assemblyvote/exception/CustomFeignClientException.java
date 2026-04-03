package br.com.sicred.assemblyvote.exception;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class CustomFeignClientException extends RuntimeException {

    private final Instant timestamp;
    private final String path;
    private final String method;
    private final String origin;
    private final Integer status;
    private final String code;
    private final String message;
    private final byte[] body;
    private final String trace;
    private final String span;
    private final List<ErrorDetail> errors;
    private final CustomFeignClientException other;
}
