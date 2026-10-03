package com.example.tickets.exception;

/** Thrown for semantically invalid requests (bad query params, empty PATCH body...). Maps to HTTP 400. */
public class InvalidRequestException extends RuntimeException {

    private final String field;

    public InvalidRequestException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
