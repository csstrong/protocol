package com.wayeal.cloud.exception;

/**
 * @author jian
 * @version 2023-05-29 15:46
 */
public class MessageParsingException extends RuntimeException {

    public MessageParsingException() { }

    public MessageParsingException(String message) {
        super(message);
    }

    public MessageParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageParsingException(Throwable cause) {
        super(cause);
    }
}
