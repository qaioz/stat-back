package com.gaioz.stats.exception;

import jakarta.persistence.OptimisticLockException;
import org.hibernate.StaleObjectStateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({StaleObjectStateException.class, OptimisticLockException.class})
    public ResponseEntity<String> handleOptimisticLocking(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Concurrent modification detected: " + ex.getMessage());
    }

}
