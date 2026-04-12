package com.alterronix.alteronixback.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.alterronix.alteronixback.enums.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Gestion des erreurs générales
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handlerGlobalException(Exception ex){
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Une erreur inattendue s'est produite",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Gestion des erreurs spécifiques à une ressource non trouvée
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlerResourceNotFound(ResourceNotFoundException ex){
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("statusCode", ex.getStatusCode() !=null ? ex.getStatusCode(): String.valueOf(HttpStatus.NOT_FOUND.value()));
        errorResponse.put("status", Status.FAILED.name());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    // Gestion des erreurs d'indentification
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handlerBadCredentials(BadCredentialsException ex){
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("statusCode", ex.getStatusCode() !=null ? ex.getStatusCode(): String.valueOf(HttpStatus.UNAUTHORIZED.value()));
        errorResponse.put("status", Status.FAILED.name());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(InvalidResourceException.class)
    public ResponseEntity<Map<String, String>> handleInvalidResource(InvalidResourceException ex){
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("statusCode", ex.getStatusCode() !=null ? ex.getStatusCode(): String.valueOf(HttpStatus.UNAUTHORIZED.value()));
        errorResponse.put("status", Status.FAILED.name());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    // Gestion des erreurs de validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handlerValidationExceptions(MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
