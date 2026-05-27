package com.darshana.pipeline.exception;

import com.darshana.pipeline.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralized exception handling — catches all exceptions and returns
 * structured JSON error responses instead of default Spring error pages.
 *
 * Interview point: "@RestControllerAdvice combines @ControllerAdvice and
 * @ResponseBody — it applies globally to all controllers."
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 - Resource not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build()
        );
    }

    // 400 - Invalid stage transition (business rule violation)
    @ExceptionHandler(InvalidStageTransitionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidTransition(
            InvalidStageTransitionException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiErrorResponse.builder()
                .status(400)
                .error("Invalid Stage Transition")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build()
        );
    }

    // 400 - Bean Validation failures (@NotBlank, @Email, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<String> details = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiErrorResponse.builder()
                .status(400)
                .error("Validation Failed")
                .message("One or more fields are invalid")
                .details(details)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build()
        );
    }

    // 500 - Catch-all for unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiErrorResponse.builder()
                .status(500)
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again.")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build()
        );
    }
}
