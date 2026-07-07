package com.fooddelivery.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single @RestControllerAdvice that catches every exception in the application.
 *
 * ── Response shape (ALL errors) ──────────────────────────────────────────────
 * {
 *   "timestamp" : "2025-01-15T10:30:00",   // ISO-8601 LocalDateTime
 *   "status"    : 404,                      // numeric HTTP code
 *   "error"     : "Not Found",              // HTTP reason phrase
 *   "message"   : "Customer with id 5 not found",
 *   "path"      : "/api/customers/5"
 * }
 *
 * ── Validation-only addition ──────────────────────────────────────────────────
 * {
 *   ...above fields...,
 *   "fieldErrors" : {
 *     "email"     : "must be a valid email",
 *     "firstName" : "must not be blank"
 *   }
 * }
 *
 * fieldErrors is OMITTED (not null, not present) for all non-validation errors.
 * No stack trace is ever leaked in any response.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ═════════════════════════════════════════════════════════════════════════
    // Body builder — standard shape (no fieldErrors key)
    // ═════════════════════════════════════════════════════════════════════════

    private Map<String, Object> buildBody(HttpStatus status, String message, String path) {
        // LinkedHashMap preserves insertion order → predictable JSON key ordering
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    status.value());
        body.put("error",     status.getReasonPhrase());
        body.put("message",   message);
        body.put("path",      path);
        return body;
    }

    // Validation variant — adds fieldErrors map
    private Map<String, Object> buildValidationBody(HttpStatus status, String message,
                                                    String path,
                                                    Map<String, String> fieldErrors) {
        Map<String, Object> body = buildBody(status, message, path);
        body.put("fieldErrors", fieldErrors);   // present ONLY for validation failures
        return body;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 404 — Resource not found / soft-deleted
    // ═════════════════════════════════════════════════════════════════════════

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest req) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildBody(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI()));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 404 — Spring's own "no handler mapped" (unknown URL)
    // ═════════════════════════════════════════════════════════════════════════

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandler(
            NoResourceFoundException ex, HttpServletRequest req) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildBody(HttpStatus.NOT_FOUND,
                        "No endpoint found for: " + req.getMethod() + " " + req.getRequestURI(),
                        req.getRequestURI()));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 409 — Invalid order / payment state transition
    // ═════════════════════════════════════════════════════════════════════════

    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidState(
            InvalidOrderStateException ex, HttpServletRequest req) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildBody(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI()));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 409 — Duplicate active resource
    // ═════════════════════════════════════════════════════════════════════════

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(
            DuplicateResourceException ex, HttpServletRequest req) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildBody(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI()));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 400 — Bean Validation failure (@Valid on @RequestBody)
    //        fieldErrors map is ONLY added here
    // ═════════════════════════════════════════════════════════════════════════

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        // Collect ALL field errors — later error for the same field wins
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildValidationBody(
                        HttpStatus.BAD_REQUEST,
                        "Validation failed — " + fieldErrors.size()
                                + " field error(s). See 'fieldErrors' for details.",
                        req.getRequestURI(),
                        fieldErrors));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 400 — Malformed / unreadable JSON body
    // ═════════════════════════════════════════════════════════════════════════

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(
            HttpMessageNotReadableException ex, HttpServletRequest req) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildBody(HttpStatus.BAD_REQUEST,
                        "Request body is missing or contains malformed JSON.",
                        req.getRequestURI()));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 400 — Wrong type for path variable or request param (e.g. "abc" for Integer id)
    // ═════════════════════════════════════════════════════════════════════════

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String expected = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName() : "unknown";
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildBody(HttpStatus.BAD_REQUEST,
                        "Parameter '" + ex.getName() + "' must be of type " + expected
                                + " but received: '" + ex.getValue() + "'.",
                        req.getRequestURI()));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 400 — Required @RequestParam is missing
    // ═════════════════════════════════════════════════════════════════════════

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest req) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildBody(HttpStatus.BAD_REQUEST,
                        "Required request parameter '" + ex.getParameterName()
                                + "' of type " + ex.getParameterType() + " is missing.",
                        req.getRequestURI()));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 405 — HTTP method not allowed (GET instead of POST, etc.)
    // ═════════════════════════════════════════════════════════════════════════

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(buildBody(HttpStatus.METHOD_NOT_ALLOWED,
                        "HTTP method '" + ex.getMethod()
                                + "' is not supported for this endpoint.",
                        req.getRequestURI()));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 500 — Catch-all for anything unhandled above
    //        NEVER leaks stack trace or internal details
    // ═════════════════════════════════════════════════════════════════════════

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(
            Exception ex, HttpServletRequest req) {
        // Log internally (in a real system use a proper logger)
        System.err.println("[GlobalExceptionHandler] Unhandled exception at "
                + req.getRequestURI() + " : " + ex.getClass().getName()
                + " — " + ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildBody(HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected internal error occurred. Please try again later.",
                        req.getRequestURI()));
    }
}