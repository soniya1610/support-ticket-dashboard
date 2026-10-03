package com.example.tickets.exception;

import com.example.tickets.dto.ErrorResponse;
import com.example.tickets.dto.ErrorResponse.FieldDetail;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldDetail(fe.getField(), fe.getDefaultMessage()))
                .distinct()
                .sorted(Comparator.comparing(FieldDetail::field).thenComparing(FieldDetail::message))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", details);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException ex) {
        List<FieldDetail> details = ex.getField() == null
                ? List.of()
                : List.of(new FieldDetail(ex.getField(), ex.getMessage()));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        Throwable t = ex;
        while (t != null && !(t instanceof JsonMappingException)) {
            t = t.getCause();
        }
        if (t instanceof InvalidFormatException ife) {
            String field = lastFieldName(ife);
            Class<?> target = ife.getTargetType();
            String message = target != null && target.isEnum()
                    ? "Invalid value '" + ife.getValue() + "'. Allowed values: " + Arrays.toString(target.getEnumConstants())
                    : "Invalid value '" + ife.getValue() + "'";
            return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid value for field '" + field + "'",
                    field == null ? List.of() : List.of(new FieldDetail(field, message)));
        }
        if (t instanceof UnrecognizedPropertyException upe) {
            String field = upe.getPropertyName();
            return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Unknown field '" + field + "'",
                    List.of(new FieldDetail(field, "Unknown field")));
        }
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Request body is missing or is not valid JSON", List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Class<?> required = ex.getRequiredType();
        String message = required != null && required.isEnum()
                ? "Invalid value '" + ex.getValue() + "'. Allowed values: " + Arrays.toString(required.getEnumConstants())
                : "Invalid value '" + ex.getValue() + "'";
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid value for parameter '" + ex.getName() + "'",
                List.of(new FieldDetail(ex.getName(), message)));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Missing parameter '" + ex.getParameterName() + "'",
                List.of(new FieldDetail(ex.getParameterName(), "Parameter is required")));
    }

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TicketNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", "Resource not found", List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", ex.getMessage(), List.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMedia(HttpMediaTypeNotSupportedException ex) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", "Content type must be application/json", List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred", List.of());
    }

    private static String lastFieldName(JsonMappingException ex) {
        List<JsonMappingException.Reference> path = ex.getPath();
        return path.isEmpty() ? null : path.get(path.size() - 1).getFieldName();
    }

    private static ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message, List<FieldDetail> details) {
        return ResponseEntity.status(status).body(ErrorResponse.of(code, message, details));
    }
}
