package br.com.sicred.assemblyvote.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractException extends RuntimeException {

    private String code;
    private String message;

    private AbstractException() {
    }

    protected AbstractException(final String code, final String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
