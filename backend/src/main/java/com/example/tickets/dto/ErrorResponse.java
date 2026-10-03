package com.example.tickets.dto;

import java.util.List;

public record ErrorResponse(ErrorBody error) {

    public record ErrorBody(String code, String message, List<FieldDetail> details) {
    }

    public record FieldDetail(String field, String message) {
    }

    public static ErrorResponse of(String code, String message, List<FieldDetail> details) {
        return new ErrorResponse(new ErrorBody(code, message, details));
    }
}
