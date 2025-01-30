package org.example.exception;

public class InvalidDataFormatException extends RuntimeException {
    public InvalidDataFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
