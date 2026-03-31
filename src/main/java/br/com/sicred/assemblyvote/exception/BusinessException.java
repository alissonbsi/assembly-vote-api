package br.com.sicred.assemblyvote.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class BusinessException extends AbstractException {

    private static final String CODE = "BUSINESS";

    public BusinessException(final String message) {
        super(CODE, message);
    }

    public BusinessException(final String code, final String message) {
        super(code, message);
    }
}
