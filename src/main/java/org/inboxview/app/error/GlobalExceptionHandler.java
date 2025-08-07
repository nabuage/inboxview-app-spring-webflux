package org.inboxview.app.error;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException exception) {
        HttpStatus status = getRuntimeExceptionHttpStatus(exception);

        return ResponseEntity
            .status(status)
            .body(
                new ErrorResponse(createErrorId(), exception.getMessage(), status.value())
            );
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleBadCredenialsxception(BadCredentialsException exception) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        return ResponseEntity
            .status(status)
            .body(
                new ErrorResponse(createErrorId(), exception.getMessage(), status.value())
            );
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleDuplicationException(DuplicateException exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity
            .status(status)
            .body(
                new ErrorResponse(createErrorId(), exception.getMessage(), status.value())
            );
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException exception) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        return ResponseEntity
            .status(status)
            .body(
                new ErrorResponse(createErrorId(), exception.getMessage(), status.value())
            );
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ex.getBindingResult().getFieldErrors().stream()
                                .collect(Collectors.toMap(FieldError::getField, error -> Optional.ofNullable(error.getDefaultMessage()).orElse("")))
                );
    }

    private HttpStatus getRuntimeExceptionHttpStatus(RuntimeException exception) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        if (exception instanceof DataException) {
            httpStatus = HttpStatus.BAD_REQUEST;
        }
        else if (exception instanceof AuthorizationDeniedException) {
            httpStatus = HttpStatus.FORBIDDEN;
        }

        return httpStatus;
    }

    private String createErrorId() {
        return UUID.randomUUID().toString();
    }
}
