package br.com.sicred.assemblyvote.api.handler;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class CustomErrorResponse {

    private Instant timestamp;
    private String path;
    private String method;
    private String origin;
    private Integer status;
    private String code;
    private String message;
    private byte[] body;
    private String trace;
    private String span;
    private List<ErrorDetail> errors;
    private CustomErrorResponse other;
}
