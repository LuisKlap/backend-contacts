package com.uex.contacts.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> body = Map.of("error", "MethodArgumentNotValidException", "message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException ex) {
    Map<String, String> body = Map.of("error", "BadRequestException", "message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
    Map<String, String> body = Map.of("error", "ResourceNotFoundException", "message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<Map<String, String>> handleConflict(ConflictException ex) {
    Map<String, String> body = Map.of("error", "ConflictException", "message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {
    Map<String, String> body = Map.of("error", "InvalidCredentialsException", "message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
  }

  @ExceptionHandler(OperationNotAllowedException.class)
  public ResponseEntity<Map<String, String>> handleOperationNotAllowed(OperationNotAllowedException ex) {
    Map<String, String> body = Map.of("error", "OperationNotAllowedException", "message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
  }

  @ExceptionHandler(ExternalServiceException.class)
  public ResponseEntity<Map<String, String>> handleExternalService(ExternalServiceException ex) {
    Map<String, String> body = Map.of("error", "ExternalServiceException", "message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleAll(Exception ex) {
    Map<String, String> body = Map.of("error", ex.getClass().getSimpleName(), "message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }
}
