package com.example.kambaapijava.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException e, HttpServletRequest r) {
        var d = e.getBindingResult().getFieldErrors().stream().map(x -> x.getField() + ": " + x.getDefaultMessage()).toList();
        return response(400, "Bad Request", "Validación fallida", r, d);
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    ResponseEntity<?> credentials(Exception e, HttpServletRequest r) {
        return response(401, "Unauthorized", "Credenciales inválidas", r, List.of());
    }

    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    ResponseEntity<?> data(org.springframework.dao.DataAccessException e, HttpServletRequest r) {
        String m = e.getMostSpecificCause().getMessage();
        int s = m != null && m.contains("no encontrado") ? 404 : m != null && (m.contains("duplicad") || m.contains("existe")) ? 409 : m != null && m.contains("45000") ? 400 : 500;
        return response(s, HttpStatus.valueOf(s).getReasonPhrase(), s == 500 ? "Error interno" : m, r, List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<?> other(Exception e, HttpServletRequest r) {
        return response(400, "Bad Request", e.getMessage() == null ? "Solicitud inválida" : e.getMessage(), r, List.of());
    }

    private ResponseEntity<?> response(int s, String err, String msg, HttpServletRequest r, List<?> d) {
        return ResponseEntity.status(s).body(Map.of("timestamp", Instant.now(), "status", s, "error", err, "message", msg, "path", r.getRequestURI(), "details", d));
    }
}
