package com.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest; //  IP и URL received
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // creation of logger
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // error handlers if the password is short
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        
        // log warning about validation failure
        logger.warn("Validation failed: {}", errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // 3. process RuntimeException (400 Bad Request)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        
        // also log the client's IP address and the request path for security auditing**
        logger.error("Runtime Error: {} | Path: {} | IP: {}", 
            ex.getMessage(), 
            request.getRequestURI(), 
            request.getRemoteAddr());

        // return json 
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
    
    //  (500 Internal Server Error)
    // new secure
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex, HttpServletRequest request) {
        // ex for stack trace logging admin only
        logger.error("Critical System Error: {} | Path: {} | IP: {}", 
            ex.getMessage(), 
            request.getRequestURI(), 
            request.getRemoteAddr(),
            ex); 

        // generic message for client
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
    }
}