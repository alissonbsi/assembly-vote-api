package br.com.sicred.assemblyvote.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends AbstractException {

    private static final String CODE = "BAD_REQUEST";

    public BadRequestException(final String message) {
        super(CODE, message);
    }
}
