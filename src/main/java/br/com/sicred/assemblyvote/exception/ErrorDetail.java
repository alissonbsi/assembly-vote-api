package br.com.sicred.assemblyvote.exception;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ErrorDetail implements Serializable {
    private String field;
    private String code;
    private String message;
}
